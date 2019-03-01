package service

import io.ktor.features.NotFoundException
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient
import me.sargunvohra.lib.pokekotlin.model.LocationArea
import model.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class PokemonService {
    private val pokeApi = PokeApiClient()

    fun getPokemon(id: Int): Pokemon {
//        val pokeApi = PokeApiClient()

//        val pokemonOwner = Users.getUser(userId)

        val pokemon = transaction {
            Pokemons.select{Pokemons.id eq id}.firstOrNull()
            } ?: throw NotFoundException("No Pokemon with ID '$id' exists")

        return Pokemons.toPokemon(pokemon)
    }

}