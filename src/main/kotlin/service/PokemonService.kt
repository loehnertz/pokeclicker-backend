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
        val pokemonRow = transaction {
            Pokemons.select{Pokemons.id eq id}.firstOrNull()
            } ?: throw NotFoundException("No Pokemon with ID '$id' exists")

        val pokemon = Pokemons.toPokemon(pokemonRow)

        return Pokemon(
            id = pokemon.id,
            pokeNumber = pokemon.pokeNumber,
            owner = pokemon.owner,
            xp = pokemon.xp,
            aquisitionDateTime = pokemon.aquisitionDateTime,
            apiInfo = PokeApi.client.getPokemon(id)
        )
    }

    fun getPokedexPokemon(id: Int): me.sargunvohra.lib.pokekotlin.model.Pokemon {
        val pokemon = PokeApi.client.getPokemon(id)

        return pokemon
    }

}