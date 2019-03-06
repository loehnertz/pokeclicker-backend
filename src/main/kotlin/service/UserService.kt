package service

import model.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class UserService {

    fun getUserPokemon(userId: Int): List<model.Pokemon> {

        val ownedPokemons = transaction {
            Pokemons.select{Pokemons.owner eq userId}.map { Pokemons.toPokemon(it) }
        }

        return ownedPokemons
    }


}