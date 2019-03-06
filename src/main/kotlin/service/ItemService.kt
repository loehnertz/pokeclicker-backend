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
        val item = transaction {
            Items.select{Items.id eq id}.firstOrNull()
        } ?: throw NotFoundException("No Item with ID '$id' exists")

        return Items.toItem(item)
    }

    fun getApiItem(id: Int): me.sargunvohra.lib.pokekotlin.model.Item {
        val item = PokeApi.client.getItem(id)

        return item
    }

}