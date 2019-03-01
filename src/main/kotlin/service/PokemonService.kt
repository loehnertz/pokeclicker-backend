package service

import me.sargunvohra.lib.pokekotlin.client.PokeApiClient
import me.sargunvohra.lib.pokekotlin.model.LocationArea
import model.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class PokemonService {
    private val pokeApi = PokeApiClient()

    fun getOwnedPokemon(id: Int): Pokemon {
//        val pokeApi = PokeApiClient()

//        val pokemonOwner = Users.getUser(userId)

        val pokemon = transaction {
            Pokemons.select{Pokemons.id eq id}.firstOrNull()
        }

        return Pokemons.toPokemon(pokemon!!)
    }

}