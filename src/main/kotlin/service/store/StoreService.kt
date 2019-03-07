package service.store

import io.ktor.features.BadRequestException
import me.sargunvohra.lib.pokekotlin.model.Pokemon
import me.sargunvohra.lib.pokekotlin.model.PokemonEncounter
import model.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import service.user.balance.BalanceIncreaseRateManager
import utility.PokeApi

const val BoosterpackSize = 5

class StoreService {
    fun getAllBoosterpacks(): List<Boosterpack> {
        val locationAreas = PokeApi.client.getLocationAreaList(0, 55).results

        return locationAreas.map { getSpecificBoosterpack(it.id) }
    }

    fun getSpecificBoosterpack(id: Int): Boosterpack {
        val locationArea = PokeApi.getLocationArea(id)

        return Boosterpack(
            name = capitalizeLocationName(locationArea.name),
            price = determineBoosterpackPrice(locationArea.gameIndex),
            locationAreaId = locationArea.id,
            hexColor = determineHexColorBasedOnLocationName(locationArea.gameIndex)
        )
    }

    fun buyBoosterpack(boosterpackId: Int, user: User): List<model.Pokemon> {
        // Retrieve the information necessary to open a new boosterpack
        val boosterpack = getSpecificBoosterpack(boosterpackId)
        val locationArea = PokeApi.getLocationArea(boosterpackId)

        // Check if user has enough money
        if (user.pokeDollars < boosterpack.price) throw BadRequestException("User does not own enough PokéDollars")

        // Open the booster pack
        val receivedPokemons = openBoosterpack(locationArea.pokemonEncounters)

        // Insert the received Pokemon into the database
        val insertedPokemons = insertReceivedPokemon(receivedPokemons, user)

        // Subtract the booster pack price from the user's account balance
        Users.subtractPokeDollarsFromBalance(user.id, boosterpack.price)

        BalanceIncreaseRateManager(user).updateIncreaseRate()

        return insertedPokemons
    }

    private fun openBoosterpack(pokemonEncounters: List<PokemonEncounter>): List<Pokemon> {
        val pokemons = pokemonEncounters.map { PokeApi.getPokemon(it.pokemon.id) }.sortedBy { it.baseExperience }.asReversed()

        val possiblePokemons = arrayListOf<Pokemon>()
        pokemons.forEachIndexed { index, pokemon -> repeat(index + 1) { possiblePokemons.add(pokemon) } }

        return possiblePokemons.shuffled().take(BoosterpackSize)
    }

    private fun insertReceivedPokemon(drawnPokemons: List<Pokemon>, user: User): List<model.Pokemon> {
        val receivedPokemons = arrayListOf<model.Pokemon>()

        transaction {
            for (pokemon in drawnPokemons) {
                val insertedPokemon = Pokemons.insert {
                    it[pokeNumber] = pokemon.id
                    it[owner] = user.id
                    it[xp] = pokemon.baseExperience
                    it[aquisitionDateTime] = DateTime()
                }

                receivedPokemons.add(
                    model.Pokemon(
                        id = insertedPokemon.resultedValues!!.first()[Pokemons.id],
                        pokeNumber = pokemon.id,
                        xp = pokemon.baseExperience,
                        aquisitionDateTime = DateTime(),
                        apiInfo = pokemon
                    )
                )
            }
        }

        return receivedPokemons
    }

    private fun capitalizeLocationName(locationName: String): String {
        return locationName.split("-").joinToString(" ") { it.capitalize() }
    }

    private fun determineBoosterpackPrice(gameIndex: Int): Long {
        return Math.pow(gameIndex.toDouble(), 2.0).toLong()
    }

    private fun determineHexColorBasedOnLocationName(gameIndex: Int): String {
        var s0 = (Math.sqrt(5.0) * gameIndex * 0x1000000).toLong()
        val s1 = gameIndex.toLong()

        s0 = s0 xor (s0 shl 23)
        s0 = s0 xor s0.ushr(17)
        s0 = s0 xor (s1 xor s1.ushr(26))
        val col = (s0 + s1) and 0xffffff

        return col.toString(16).padStart(6, '0')
    }
}
