package service.user.pokemon

import com.google.gson.Gson
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient
import me.sargunvohra.lib.pokekotlin.model.ChainLink
import me.sargunvohra.lib.pokekotlin.model.PokemonSpecies
import model.Pokemon
import model.Pokemons
import model.User
import model.toPokemon
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import service.store.data.ThinPokemon
import service.user.balance.BalanceIncreaseRateManager
import service.user.data.UserPokemonMergeRequest
import utility.PokeApi
import utility.RedisConnector
import java.math.BigDecimal
import java.math.RoundingMode.CEILING
import java.util.*
import kotlin.random.Random

class PokemonMerger(private val user: User) {
    fun mergePokemons(mergeRequest: UserPokemonMergeRequest): Pokemon {
        val selectedPokemons = retrieveUserPokemons(mergeRequest.pokemonIds)

        checkValidity(selectedPokemons)

        val evolutionPokemon = retrieveEvolutionPokemon(selectedPokemons.first())

        return removeMergedPokemonAndGiveNewOneToUser(selectedPokemons, evolutionPokemon)
    }

    private fun retrieveUserPokemons(pokemonIds: List<Int>): List<Pokemon> {
        val selectedPokemons = arrayListOf<Pokemon>()

        pokemonIds.forEach {
            selectedPokemons.add(Pokemons.toPokemon(transaction { Pokemons.select { Pokemons.id eq it }.first() }))
        }

        return selectedPokemons
    }

    private fun removeMergedPokemonAndGiveNewOneToUser(selectedPokemons: List<Pokemon>, evolutionPokemon: ThinPokemon): Pokemon {
        val combinedExperiencePoints = calculateCombinedExperiencePointsOfSelectedPokemons(selectedPokemons)
        val experiencePointsOfNewPokemon = determineExperiencePointsOfNewPokemon(combinedExperiencePoints)

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

        BalanceIncreaseRateManager(user).updateIncreaseRate(experiencePointsOfNewPokemon - combinedExperiencePoints)

        return model.Pokemon(
            id = insertedPokemon.resultedValues!!.first()[Pokemons.id],
            pokeNumber = evolutionPokemon.id,
            xp = experiencePointsOfNewPokemon,
            aquisitionDateTime = DateTime(),
            thinApiInfo = PokeApi().getPokemon(evolutionPokemon.id)
        )
    }

    private fun calculateCombinedExperiencePointsOfSelectedPokemons(selectedPokemons: List<Pokemon>): BigDecimal {
        return selectedPokemons.map { it.xp }.fold(BigDecimal(0)) { sum, xp -> sum.add(xp) }
    }

    private fun determineExperiencePointsOfNewPokemon(combinedExperiencePoints: BigDecimal): BigDecimal {
        return (combinedExperiencePoints * (Random.nextDouble() + 1).toBigDecimal()).setScale(0, CEILING)
    }

    private fun retrieveEvolutionPokemon(selectedPokemon: Pokemon): ThinPokemon {
        val cachedValue = RedisConnector().hmget(RedisKeyEvolutions, selectedPokemon.pokeNumber.toString()).firstOrNull()
        if (cachedValue != null) return gson.fromJson(cachedValue, ThinPokemon::class.java)

        val evolutionPokemon = retrieveNonCachedEvolutionPokemon(selectedPokemon)

        val thinPokemon = ThinPokemon(
            id = evolutionPokemon.id,
            name = evolutionPokemon.name.capitalize(),
            xp = evolutionPokemon.baseExperience.toBigDecimal(),
            sprite = evolutionPokemon.sprites.frontDefault ?: evolutionPokemon.sprites.frontShiny
        )

        RedisConnector().hmset(RedisKeyEvolutions, mapOf(selectedPokemon.pokeNumber.toString() to gson.toJson(thinPokemon)))

        return thinPokemon
    }

    private fun retrieveNonCachedEvolutionPokemon(selectedPokemon: Pokemon): me.sargunvohra.lib.pokekotlin.model.Pokemon {
        val pokemonSpecies = client.getPokemon(selectedPokemon.pokeNumber).species
        val pokemonEvolutionChain = client.getPokemonSpecies(pokemonSpecies.id).evolutionChain
        val pokemonEvolutionSpecies = traverseEvolutionChain(client.getEvolutionChain(pokemonEvolutionChain.id).chain, selectedPokemon)
        return client.getPokemon(pokemonEvolutionSpecies.varieties.first().pokemon.id)
    }

    private fun traverseEvolutionChain(chain: ChainLink, selectedPokemon: Pokemon): PokemonSpecies {
        var evolvesInto = chain.evolvesTo.firstOrNull()
            ?: throw NoSuchElementException("The selected Pokémon are the last link of their evolution chain")

        while (evolvesInto.species.name == selectedPokemon.thinApiInfo!!.name.toLowerCase()) {
            evolvesInto = evolvesInto.evolvesTo.firstOrNull()
                ?: throw NoSuchElementException("The selected Pokémon are the last link of their evolution chain")
        }

        return client.getPokemonSpecies(evolvesInto.species.id)
    }

    private fun checkValidity(selectedPokemons: List<Pokemon>) {
        if (selectedPokemons.size < minimumMergeAmount) throw IllegalArgumentException("You need to selct at least $minimumMergeAmount Pokémon")
        if (selectedPokemons.map { it.pokeNumber }.toSet().size > 1) throw IllegalArgumentException("The selected Pokemon are not all the same")
        if (!selectedPokemons.all { it.owner!!.name == user.name }) throw IllegalAccessException("The given IDs are not all owned by the user")
    }

    companion object {
        private val gson = Gson()
        private val client = PokeApiClient()
        private const val minimumMergeAmount = 3
        const val RedisKeyEvolutions = "evolutions"
    }
}
