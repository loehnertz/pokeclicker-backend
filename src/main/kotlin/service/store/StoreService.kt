package service.store

import com.google.gson.Gson
import io.ktor.features.BadRequestException
import io.ktor.features.NotFoundException
import model.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import service.store.data.ThinPokemon
import service.user.balance.BalanceIncreaseRateManager
import service.user.balance.BalanceIncreaseRateManager.Companion.IncreaseRateScalingFactor
import service.user.balance.BalanceManager
import utility.PokeApi
import utility.RedisConnector
import java.math.BigDecimal
import java.math.RoundingMode.CEILING
import kotlin.random.Random

const val BoosterpackSize = 5
const val LocationIdBaseIncrease = 3.0
const val SecondsInFiveMinutes = 300
const val BoosterpackAmountLimit = 55
const val RedisKeyBoosterpacks = "boosterpacks"
const val RedisKeyBoosterpackIds = "boosterpack_ids"
val LegendaryPokemon = intArrayOf(144, 145, 146, 150, 151, 243, 244, 245, 249, 250, 251, 377, 378, 379, 380, 381, 382, 383, 384, 385, 386, 480, 481, 482, 483, 484, 485, 486, 487, 488, 489, 490, 491, 492, 493, 494, 638, 639, 640, 641, 642, 643, 644, 645, 646, 647, 648, 649)
const val LegendaryPokemonXPMultiplier = 15
const val LegendaryPokemonInBoosterPack = 6 //Once every LegendaryPokemonInBoosterPack times there is a legendary pokemon in a booster pack. Please note that this doesn't mean that you'll also draw this pokemon.

class StoreService {
    private val gson = Gson()

    fun getAllBoosterpacks(): List<Boosterpack> {
        val cachedBoosterpackIds = RedisConnector().smembers(RedisKeyBoosterpackIds)
        if (cachedBoosterpackIds.size == BoosterpackAmountLimit) return cachedBoosterpackIds.mapNotNull { getSpecificBoosterpack(it.toInt()) }

        val boosterpackIds = PokeApi().getLocationIdList(BoosterpackAmountLimit)
        RedisConnector().sadd(RedisKeyBoosterpackIds, *boosterpackIds.map { it.toString() }.toTypedArray())

        return boosterpackIds.mapNotNull { getSpecificBoosterpack(it) }
    }

    fun getSpecificBoosterpack(id: Int): Boosterpack? {
        val cachedValue = RedisConnector().hmget(RedisKeyBoosterpacks, id.toString()).firstOrNull()
        if (cachedValue != null) return gson.fromJson(cachedValue, Boosterpack::class.java)

        val location = PokeApi().getLocation(id)
        val pokemonsInLocation = PokeApi().getPokemonsOfLocation(location)
        if (pokemonsInLocation.isEmpty()) return null

        val boosterpack = Boosterpack(
            name = capitalizeLocationName(location.name),
            price = determineBoosterpackPrice(location.id),
            locationId = location.id,
            hexColor = determineHexColorBasedOnLocationId(location.id),
            pokemons = pokemonsInLocation
        )

        RedisConnector().hmset(RedisKeyBoosterpacks, mapOf(id.toString() to gson.toJson(boosterpack)))

        return boosterpack
    }

    fun buyBoosterpack(id: Int, user: User): List<model.Pokemon> {
        // Retrieve the information necessary to open a new boosterpack
        val boosterpack = getSpecificBoosterpack(id) ?: throw NotFoundException("This boosterpack does not exist")

        // Retrieve current balance of user
        val balance = BalanceManager(user).retrieveCurrentBalance()

        // Check if user has enough money
        if (balance < boosterpack.price) throw BadRequestException("User does not own enough PokéDollars")

        // Open the booster pack
        val receivedPokemons = openBoosterpack(boosterpack.pokemons, boosterpack.price)

        // Insert the received Pokemon into the database
        val insertedPokemons = insertReceivedPokemon(receivedPokemons, user)

        // Subtract the booster pack price from the user's account balance
        Users.subtractPokeDollarsFromBalance(user.id, boosterpack.price)

        // Update the gather rate of the user
        BalanceIncreaseRateManager(user).updateIncreaseRate(receivedPokemons.map { it.xp }.fold(BigDecimal(0)) { sum, xp -> sum.add(xp) })

        return insertedPokemons
    }

    private fun openBoosterpack(pokemons: List<ThinPokemon>, boosterpackPrice: BigDecimal): List<ThinPokemon> {
        val sortedPokemons = pokemons.sortedBy { it.xp }.asReversed()

        val possiblePokemons = arrayListOf<ThinPokemon>()
		if(Random.nextInt(0, LegendaryPokemonInBoosterPack) == 0)
			possiblePokemons.add(PokeApi().getPokemon(LegendaryPokemon.random()));
        sortedPokemons.forEachIndexed { index, pokemon -> repeat(index + 1) { possiblePokemons.add(pokemon) } }

        val drawnPokemons = possiblePokemons.shuffled().take(BoosterpackSize)
        drawnPokemons.forEach {
            val xp = (boosterpackPrice.multiply((getReasonablyNormallyDistributedDouble() + 1).toBigDecimal()).divide((SecondsInFiveMinutes * BoosterpackSize).toBigDecimal(), CEILING)).setScale(0, CEILING)

            if (xp == BigDecimal(1) && getReasonablyNormallyDistributedDouble() > 0.55) {
                it.xp = xp + BigDecimal(1)  // Randomly increase XP by one so that the first three booster packs scale better
            } else {
                it.xp = xp
            }

            if (LegendaryPokemon.contains(it.id)) {  // Legendary pokemon are 10 times as powerful as normal pokemon :)
                it.xp *= LegendaryPokemonXPMultiplier
            }
        }

        return drawnPokemons
    }

    private fun insertReceivedPokemon(drawnPokemons: List<ThinPokemon>, user: User): List<model.Pokemon> {
        val receivedPokemons = arrayListOf<model.Pokemon>()

        transaction {
            for (pokemon in drawnPokemons) {
                val insertedPokemon = Pokemons.insert {
                    it[pokeNumber] = pokemon.id
                    it[owner] = user.id
                    it[xp] = pokemon.xp
                    it[aquisitionDateTime] = DateTime()
                }

                receivedPokemons.add(
                    model.Pokemon(
                        id = insertedPokemon.resultedValues!!.first()[Pokemons.id],
                        pokeNumber = pokemon.id,
                        xp = pokemon.xp,
                        aquisitionDateTime = DateTime(),
                        thinApiInfo = pokemon
                    )
                )
            }
        }

        return receivedPokemons
    }

    private fun capitalizeLocationName(locationName: String): String {
        return locationName.split("-").joinToString(" ") { it.capitalize() }
    }

    private fun determineBoosterpackPrice(locationId: Int): BigDecimal {
        return (BoosterpackSize * SecondsInFiveMinutes * Math.pow(LocationIdBaseIncrease, (locationId - 1).toDouble()) / IncreaseRateScalingFactor).toBigDecimal()
    }

    private fun determineHexColorBasedOnLocationId(locationId: Int): String {
        var s0 = (Math.sqrt(5.0) * locationId * 0x1000000).toLong()
        val s1 = locationId.toLong()

        s0 = s0 xor (s0 shl 23)
        s0 = s0 xor s0.ushr(17)
        s0 = s0 xor (s1 xor s1.ushr(26))
        val col = (s0 + s1) and 0xffffff

        return col.toString(16).padStart(6, '0')
    }

    private fun getReasonablyNormallyDistributedDouble(): Double {
        return ((Random.nextDouble() + Random.nextDouble() + Random.nextDouble()) / 3)
    }
}
