package service

import com.google.gson.Gson
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient
import me.sargunvohra.lib.pokekotlin.model.LocationArea
import model.Boosterpack
import redis.clients.jedis.Jedis

const val RedisHashMapKey = "locationAreas"

class StoreService {
    private val gson = Gson()
    private val pokeApi = PokeApiClient()
    private var redis = Jedis("localhost")

    fun getAllBoosterpacks(): List<Boosterpack> {
        val locationAreas = pokeApi.getLocationAreaList(0, 100).results.map { getLocationArea(it.id) }

        return locationAreas.map {
            Boosterpack(
                name = convertLocationName(it.name),
                price = determineBoosterpackPrice(it.gameIndex),
                locationAreaId = it.id,
                hexColor = determineHexColorBasedOnLocationName(it.gameIndex)
            )
        }
    }

    private fun getLocationArea(id: Int): LocationArea {
        val cachedValue = redis.hmget(RedisHashMapKey, id.toString()).firstOrNull()
        if (cachedValue != null) {
            return gson.fromJson(cachedValue, LocationArea::class.java)
        }

        val locationArea = pokeApi.getLocationArea(id)

        redis.hmset(RedisHashMapKey, mapOf(id.toString() to gson.toJson(locationArea)))

        return locationArea
    }

    private fun convertLocationName(locationName: String): String {
        return locationName.split("-").joinToString(" ") { it.capitalize() }
    }

    private fun determineBoosterpackPrice(gameIndex: Int): Long {
        return Math.pow(gameIndex.toDouble(), 2.0).toLong()
    }

    private fun determineHexColorBasedOnLocationName(gameIndex: Int): String {
        var s0 = (Math.sqrt(5.0) * gameIndex * 0x1000000).toLong()
        val s1 = gameIndex.toLong()

        s0 = s0 xor (s0 shl 23)
        s0 = s0 xor s0.ushr(17)
        s0 = s0 xor (s1 xor s1.ushr(26))
        val col = (s0 + s1) and 0xffffff

        return col.toString(16).padStart(6, '0')
    }
}
