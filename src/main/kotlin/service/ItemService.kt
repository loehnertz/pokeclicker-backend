package service

import io.ktor.features.NotFoundException
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient
import me.sargunvohra.lib.pokekotlin.model.LocationArea
import model.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import utility.PokeApi

class ItemService {

    fun getDBItem(id: Int): Item {
        val itemRow = transaction {
            Items.select{Items.id eq id}.firstOrNull()
        } ?: throw NotFoundException("No Item with ID '$id' exists")


        val item = Items.toItem(itemRow)

        return Item(
            id = item.id,
            itemNumber = item.itemNumber,
            owner = item.owner,
            aquisitionDateTime = item.aquisitionDateTime,
            apiInfo = PokeApi.client.getItem(id)
        )
    }

    fun getApiItemById(id: Int): me.sargunvohra.lib.pokekotlin.model.Item {
        return PokeApi.client.getItem(id)
    }

}