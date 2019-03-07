package utility

import io.ktor.features.NotFoundException
import model.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object PokeApiAdapter {
    fun getPokemonData(dbId: Int): Pokemon {
        val pokemon = Pokemons.toPokemon(
            transaction {
                Pokemons.select{Pokemons.id eq dbId}.firstOrNull()
            } ?: throw NotFoundException("No Pokemon with ID '$dbId' exists")
        )
        pokemon.apiInfo = PokeApi.client.getPokemon(pokemon.pokeNumber)

        return pokemon
    }

    fun getItemData(dbId: Int): Item {
        val item = Items.toItem(
            transaction {
                Items.select { Items.id eq dbId }.first()
            }
        )
        item.apiInfo = PokeApi.client.getItem(item.itemNumber)

        return item
    }
}
