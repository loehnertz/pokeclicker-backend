package service.user.pokemon

import me.sargunvohra.lib.pokekotlin.client.PokeApiClient
import model.*
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import service.user.data.UserPokemonMergeRequest
import utility.PokeApi
import java.math.BigDecimal
import java.util.*
import kotlin.random.Random

class PokemonMerger(private val user: User) {
    fun mergePokemons(mergeRequest: UserPokemonMergeRequest): Pokemon {
        val pokemonsOfUser = Users.getPokemons(user.id)
        val selectedPokemons = pokemonsOfUser.filter { mergeRequest.pokemonIds.contains(it.id) }

        checkValidity(selectedPokemons, pokemonsOfUser, mergeRequest)

        val evolutionPokemon = retrieveEvolutionPokemon(selectedPokemons)

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

    private fun retrieveEvolutionPokemon(selectedPokemons: List<Pokemon>): me.sargunvohra.lib.pokekotlin.model.Pokemon {
        val pokemonSpecies = client.getPokemon(selectedPokemons.first().pokeNumber).species
        val pokemonEvolutionChain = client.getPokemonSpecies(pokemonSpecies.id).evolutionChain
        val evolvesIntoPokemon = client.getEvolutionChain(pokemonEvolutionChain.id).chain.evolvesTo.firstOrNull()
            ?: throw NoSuchElementException("The selected Pokémon are the last link of their evolution chain")
        val evolutionPokemonSpecies = client.getPokemonSpecies(evolvesIntoPokemon.species.id)
        return client.getPokemon(evolutionPokemonSpecies.varieties.first().pokemon.id)
    }

    private fun checkValidity(selectedPokemons: List<Pokemon>, pokemonsOfUser: List<Pokemon>, mergeRequest: UserPokemonMergeRequest) {
        if (selectedPokemons.size < minimumMergeAmount) throw IllegalArgumentException("You need to selct at least $minimumMergeAmount Pokémon")
        if (!pokemonsOfUser.map { it.id }.containsAll(mergeRequest.pokemonIds)) throw IllegalAccessException("The given IDs are not all owned by the user")
        if (selectedPokemons.map { it.pokeNumber }.toSet().size > 1) throw IllegalArgumentException("The selected Pokemon are not all the same")
    }

    companion object {
        private val client = PokeApiClient()
        private const val minimumMergeAmount = 3
    }
}
