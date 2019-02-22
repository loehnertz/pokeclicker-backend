
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient
import me.sargunvohra.lib.pokekotlin.model.NamedApiResource
import me.sargunvohra.lib.pokekotlin.model.PokemonSpecies

import model.Pokemon

import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction


class PokeApiAdapter {

    data class PkmnInfo(
        val dbInfo: ResultRow,
        val dbModel: Table,
        val apiInfo: PokemonSpecies
    )

    fun getPkmnData(dbId: Int): PkmnInfo {

        val pkmn = transaction {
             Pokemon.select{Pokemon.id eq dbId}.first()
        }

        val pokeApi = PokeApiClient()
        val pkmnInfo = pokeApi.getPokemonSpecies(pkmn[Pokemon.pokeNumber])

        return PkmnInfo(dbInfo = pkmn, dbModel = Pokemon, apiInfo = pkmnInfo)
    }

    data class ItemInfo(
            val dbInfo: ResultRow,
            val dbModel: Table,
            val apiInfo: me.sargunvohra.lib.pokekotlin.model.Item
    )

    fun getItemData(dbId: Int): ItemInfo {

        val item = transaction {
            model.Item.select{ model.Item.id eq dbId}.first()
        }

        val pokeApi = PokeApiClient()
        val itemInfo = pokeApi.getItem(item[model.Item.itemNumber])

        return ItemInfo(dbInfo = item, dbModel = Pokemon, apiInfo = itemInfo)
    }

}