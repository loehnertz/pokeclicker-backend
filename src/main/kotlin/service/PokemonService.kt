package service

import io.ktor.features.NotFoundException
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient
import me.sargunvohra.lib.pokekotlin.model.LocationArea
import model.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import utility.PokeApi

class PokemonService {

    fun getDBPokemon(id: Int): Pokemon {
        val pokemon = transaction {
            Pokemons.select{Pokemons.id eq id}.firstOrNull()
            } ?: throw NotFoundException("No Pokemon with ID '$id' exists")

        return Pokemons.toPokemon(pokemon)
    }

    fun getPokedexPokemon(id: Int): me.sargunvohra.lib.pokekotlin.model.Pokemon {
        val pokemon = PokeApi.client.getPokemon(id)

        return pokemon
    }

}