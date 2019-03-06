package utility

import com.google.gson.Gson
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient
import me.sargunvohra.lib.pokekotlin.model.LocationArea
import me.sargunvohra.lib.pokekotlin.model.Pokemon
import redis.clients.jedis.Jedis

const val RedisHashMapKeyLocationAreas = "locationAreas"
const val RedisHashMapKeyPokemons = "pokemons"

object PokeApi {
    val client = PokeApiClient()

    private val gson = Gson()
    private val redis = Jedis(System.getenv("redis_host"))

    fun getLocationArea(id: Int): LocationArea {
        val cachedValue = redis.hmget(RedisHashMapKeyLocationAreas, id.toString()).firstOrNull()
        if (cachedValue != null) return gson.fromJson(cachedValue, LocationArea::class.java)

        val locationArea = client.getLocationArea(id)

        redis.hmset(RedisHashMapKeyLocationAreas, mapOf(id.toString() to gson.toJson(locationArea)))

        return locationArea
    }

    fun getPokemon(id: Int): Pokemon {
        val cachedValue = redis.hmget(RedisHashMapKeyPokemons, id.toString()).firstOrNull()
        if (cachedValue != null) return gson.fromJson(cachedValue, Pokemon::class.java)

        val pokemon = client.getPokemon(id)

        redis.hmset(RedisHashMapKeyPokemons, mapOf(id.toString() to gson.toJson(pokemon)))

        return pokemon
    }
}
