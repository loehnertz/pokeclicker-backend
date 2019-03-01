package service

import me.sargunvohra.lib.pokekotlin.client.PokeApiClient
import me.sargunvohra.lib.pokekotlin.model.LocationArea
import model.Pokemon
import model.Users
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import service.DatabaseFactory.dbQuery

class PokemonService {
    private val pokeApi = PokeApiClient()

    fun getPokemon(id: Int, userId: Int): Pokemon {
//        val pokemon = pokeApi.getPokemon(id)
        val pokeApi = PokeApiClient()

        val pokemonOwner = transaction {
//            Users.select{Users.id eq userId}.map User.toUser()
        }

        val pokemon = transaction {
            //            Users.select{Users.id eq userId}.map User.toUser()
        }

//        val pkmnInfo = pokeApi.getPokemonSpecies(pkmn[Pokemon.pokeNumber])

        return Pokemon(id = id, pokeNumber = idk, owner = pokemonOwner, xp = idk, aquisitionDateTime = pokemon.aquisitionDateTime)

//        data class Pokemon(
//                val id: Int,
//                val pokeNumber: Int,
//                val owner: Users,
//                val xp: Int,
//                val aquisitionDateTime: LocalDateTime
//        )
        return pokemon
    }

}