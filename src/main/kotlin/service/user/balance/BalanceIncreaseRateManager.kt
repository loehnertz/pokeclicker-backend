package service.user.balance

import model.User
import model.Users
import model.getPokemons
import utility.RedisConnector
import java.math.BigDecimal
import java.math.RoundingMode.CEILING

class BalanceIncreaseRateManager(val user: User) {
    fun increaseBalanceBasedOnIncreaseRate(balanceManager: BalanceManager) {
        val currentIncreaseRatePerMinute = retrieveIncreaseRate()

        if (currentIncreaseRatePerMinute != null) {
            balanceManager.increaseCurrentBalance(currentIncreaseRatePerMinute)
        } else {
            updateIncreaseRate()
        }
    }

    fun updateIncreaseRate() {
        val increaseRatePerMinute = calculateIncreaseRatePerSecond()
        RedisConnector().hmset(RedisHashMapKeyIncreaseRates, mapOf(user.name to increaseRatePerMinute.toString()))
    }

    fun updateIncreaseRate(increaseRateAddition: BigDecimal) {
        RedisConnector().hincrByFloat(RedisHashMapKeyIncreaseRates, user.name, increaseRateAddition.toDouble())
    }

    fun retrieveIncreaseRate(): BigDecimal? {
        return try {
            RedisConnector().hmget(RedisHashMapKeyIncreaseRates, user.name).firstOrNull()?.toBigDecimal()
        } catch (exception: Exception) {
            // TODO: Add logging here
            null
        }
    }

    private fun calculateIncreaseRatePerSecond(): BigDecimal {
        val pokemons = Users.getPokemons(user.id)
        return (pokemons.fold(BigDecimal(0)) { sum, pokemon -> sum.add(pokemon.xp) }.divide(IncreaseRateScalingFactor.toBigDecimal())).setScale(0, CEILING)
    }

    companion object {
        const val IncreaseRateScalingFactor = 20
        private const val RedisHashMapKeyIncreaseRates = "gatherRates"
    }
}
