package utility

import com.google.gson.Gson
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient
import me.sargunvohra.lib.pokekotlin.model.LocationArea
import me.sargunvohra.lib.pokekotlin.model.Pokemon

object PokeApi {
    private const val RedisHashMapKeyLocationAreas = "locationAreas"
    private const val RedisHashMapKeyPokemons = "pokemons"

    private val gson = Gson()

    val client = PokeApiClient()

    fun getLocationArea(id: Int): LocationArea {
        val redis = RedisFactory.retrieveRedisClient()

        val cachedValue = redis.hmget(RedisHashMapKeyLocationAreas, id.toString()).firstOrNull()
        if (cachedValue != null) return gson.fromJson(cachedValue, LocationArea::class.java)

        val locationArea = client.getLocationArea(id)

        redis.hmset(RedisHashMapKeyLocationAreas, mapOf(id.toString() to gson.toJson(locationArea)))

        redis.close()

        return locationArea
    }

    fun getPokemon(id: Int): Pokemon {
        val redis = RedisFactory.retrieveRedisClient()

        val cachedValue = redis.hmget(RedisHashMapKeyPokemons, id.toString()).firstOrNull()
        if (cachedValue != null) return gson.fromJson(cachedValue, Pokemon::class.java)

        val pokemon = client.getPokemon(id)

        redis.hmset(RedisHashMapKeyPokemons, mapOf(id.toString() to gson.toJson(pokemon)))

        redis.close()

        return pokemon
    }
}
