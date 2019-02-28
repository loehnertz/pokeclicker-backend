package utility

import model.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class PokeApiAdapter {
    fun getPokemonData(dbId: Int): Pokemon {
        val pokemon = Pokemons.toPokemon(
            transaction {
                Pokemons.select { Pokemons.id eq dbId }.first()
            }
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
