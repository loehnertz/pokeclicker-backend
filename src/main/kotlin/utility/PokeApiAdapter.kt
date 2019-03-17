package utility

import io.ktor.features.NotFoundException
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient
import model.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class PokeApiAdapter {

    fun getPokemonData(dbId: Int): Pokemon {
        val pokemon = Pokemons.toPokemon(
            transaction {
                Pokemons.select { Pokemons.id eq dbId }.firstOrNull()
            } ?: throw NotFoundException("No Pokemon with ID '$dbId' exists")
        )
        pokemon.fatApiInfo = client.getPokemon(pokemon.pokeNumber)

        return pokemon
    }

    fun getItemData(dbId: Int): Item {
        val item = Items.toItem(
            transaction {
                Items.select { Items.id eq dbId }.first()
            }
        )
        item.apiInfo = client.getItem(item.itemNumber)

        return item
    }

    companion object {
        private val client = PokeApiClient()
    }
}
