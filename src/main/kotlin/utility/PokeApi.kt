package utility

import com.google.gson.Gson
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient
import me.sargunvohra.lib.pokekotlin.model.Location
import me.sargunvohra.lib.pokekotlin.model.LocationArea
import service.store.data.ThinPokemon

class PokeApi {
    private val gson = Gson()

    fun getPokemon(id: Int): ThinPokemon {
        val cachedValue = RedisConnector().hmget(RedisHashMapKeyPokemons, id.toString()).firstOrNull()
        if (cachedValue != null) return gson.fromJson(cachedValue, ThinPokemon::class.java)

        val pokemon = client.getPokemon(id)
        val thinPokemon = ThinPokemon(
            id = pokemon.id,
            name = pokemon.name.capitalize(),
            xp = pokemon.baseExperience.toLong(),
            sprite = pokemon.sprites.frontDefault ?: pokemon.sprites.frontShiny
        )

        RedisConnector().hmset(RedisHashMapKeyPokemons, mapOf(id.toString() to gson.toJson(thinPokemon)))

        return thinPokemon
    }

    fun getLocationIdList(limit: Int): List<Int> {
        return client.getLocationList(0, limit).results.map { it.id }
    }

    fun getLocation(id: Int): Location {
        val cachedValue = RedisConnector().hmget(RedisHashMapKeyLocations, id.toString()).firstOrNull()
        if (cachedValue != null) return gson.fromJson(cachedValue, Location::class.java)

        val location = client.getLocation(id)

        RedisConnector().hmset(RedisHashMapKeyLocations, mapOf(id.toString() to gson.toJson(location)))

        return location
    }

    fun getPokemonsOfLocation(location: Location): List<ThinPokemon> {
        val locationAreas = location.areas.map { getLocationArea(it.id) }
        return locationAreas.flatMap { la -> la.pokemonEncounters.map { pe -> getPokemon(pe.pokemon.id) } }.distinctBy { it.id }
    }

    private fun getLocationArea(id: Int): LocationArea {
        val cachedValue = RedisConnector().hmget(RedisHashMapKeyLocationAreas, id.toString()).firstOrNull()
        if (cachedValue != null) return gson.fromJson(cachedValue, LocationArea::class.java)

        val locationArea = client.getLocationArea(id)

        RedisConnector().hmset(RedisHashMapKeyLocationAreas, mapOf(id.toString() to gson.toJson(locationArea)))

        return locationArea
    }

    companion object {
        private val client = PokeApiClient()
        private const val RedisHashMapKeyPokemons = "pokemons"
        private const val RedisHashMapKeyLocations = "locations"
        private const val RedisHashMapKeyLocationAreas = "locationAreas"
    }
}
