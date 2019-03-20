package service.user.pokemon

import com.google.gson.Gson
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient
import model.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import service.user.data.UserPokemonMergeRequest
import utility.PokeApi
import utility.RedisConnector
import java.math.BigDecimal
import java.util.*
import kotlin.random.Random

class PokemonMerger(private val user: User) {
    fun mergePokemons(mergeRequest: UserPokemonMergeRequest): Pokemon {
        val pokemonsOfUser = Users.getPokemons(user.id)
        val selectedPokemons = pokemonsOfUser.filter { mergeRequest.pokemonIds.contains(it.id) }

        checkValidity(selectedPokemons)

        val evolutionPokemon = retrieveEvolutionPokemon(selectedPokemons.first())

        return removeMergedPokemonAndGiveNewOneToUser(selectedPokemons, evolutionPokemon)
    }

    private fun removeMergedPokemonAndGiveNewOneToUser(selectedPokemons: List<Pokemon>, evolutionPokemon: me.sargunvohra.lib.pokekotlin.model.Pokemon): Pokemon {
        val experiencePointsOfNewPokemon = determineExperiencePointsOfNewPokemon(selectedPokemons)

        val insertedPokemon = transaction {
            Pokemons.insert {
                it[pokeNumber] = evolutionPokemon.id
                it[owner] = user.id
                it[xp] = experiencePointsOfNewPokemon
                it[aquisitionDateTime] = DateTime()
            }
        }

        transaction {
            selectedPokemons.forEach { Pokemons.deleteWhere { Pokemons.id eq it.id } }
        }

        return model.Pokemon(
            id = insertedPokemon.resultedValues!!.first()[Pokemons.id],
            pokeNumber = evolutionPokemon.id,
            xp = experiencePointsOfNewPokemon,
            aquisitionDateTime = DateTime(),
            thinApiInfo = PokeApi().getPokemon(evolutionPokemon.id)
        )
    }

    private fun determineExperiencePointsOfNewPokemon(selectedPokemons: List<Pokemon>): BigDecimal {
        val combinedExperiencePoints = selectedPokemons.map { it.xp }.fold(BigDecimal(0)) { sum, xp -> sum.add(xp) }
        return (combinedExperiencePoints * (Random.nextDouble() + 1).toBigDecimal())
    }

    private fun retrieveEvolutionPokemon(selectedPokemon: Pokemon): me.sargunvohra.lib.pokekotlin.model.Pokemon {
        val cachedValue = RedisConnector().hmget(RedisKeyEvolutions, selectedPokemon.pokeNumber.toString()).firstOrNull()
        if (cachedValue != null) return gson.fromJson(cachedValue, me.sargunvohra.lib.pokekotlin.model.Pokemon::class.java)

        val pokemonSpecies = client.getPokemon(selectedPokemon.pokeNumber).species
        val pokemonEvolutionChain = client.getPokemonSpecies(pokemonSpecies.id).evolutionChain
        val evolvesIntoPokemon = client.getEvolutionChain(pokemonEvolutionChain.id).chain.evolvesTo.firstOrNull()
            ?: throw NoSuchElementException("The selected Pokémon are the last link of their evolution chain")
        val evolutionPokemonSpecies = client.getPokemonSpecies(evolvesIntoPokemon.species.id)
        val evolutionPokemon = client.getPokemon(evolutionPokemonSpecies.varieties.first().pokemon.id)

        RedisConnector().hmset(RedisKeyEvolutions, mapOf(selectedPokemon.pokeNumber.toString() to gson.toJson(evolutionPokemon)))

        return evolutionPokemon
    }

    private fun checkValidity(selectedPokemons: List<Pokemon>) {
        if (selectedPokemons.size < minimumMergeAmount) throw IllegalArgumentException("You need to selct at least $minimumMergeAmount Pokémon")
        if (selectedPokemons.map { it.pokeNumber }.toSet().size > 1) throw IllegalArgumentException("The selected Pokemon are not all the same")
        checkIfUserOwnsSelectedPokemons(selectedPokemons)
    }

    private fun checkIfUserOwnsSelectedPokemons(selectedPokemons: List<Pokemon>) {
        val validSelectedPokemonCount = transaction { Pokemons.select { (Pokemons.id inList selectedPokemons.map { it.id }) and (Pokemons.owner eq user.id) }.count() }
        if (validSelectedPokemonCount != selectedPokemons.size) throw IllegalAccessException("The given IDs are not all owned by the user")
    }

    companion object {
        private val gson = Gson()
        private val client = PokeApiClient()
        private const val minimumMergeAmount = 3
        const val RedisKeyEvolutions = "evolutions"
    }
}
