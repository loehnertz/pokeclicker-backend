package service.user.balance

import model.User
import model.Users
import model.getPokemons
import utility.RedisFactory

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
        val redis = RedisFactory.retrieveRedisClient()

        try {
            val increaseRatePerMinute = calculateIncreaseRatePerSecond()
            redis.hmset(RedisHashMapKeyIncreaseRates, mapOf(user.name to increaseRatePerMinute.toString()))
        } catch (exception: Exception) {
            // TODO: Add logging here
        } finally {
            redis.close()
        }
    }

    fun retrieveIncreaseRate(): Long? {
        val redis = RedisFactory.retrieveRedisClient()

        return try {
            redis.hmget(RedisHashMapKeyIncreaseRates, user.name).firstOrNull()?.toLong()
        } catch (exception: Exception) {
            // TODO: Add logging here
            null
        } finally {
            redis.close()
        }
    }

    private fun calculateIncreaseRatePerSecond(): Long {
        val pokemons = Users.getPokemons(user.id)
        return Math.ceil(pokemons.fold(0) { sum, pokemon -> sum + pokemon.xp }.toDouble() / IncreaseRateScalingFactor).toLong()
    }

    companion object {
        const val IncreaseRateScalingFactor = 3
        private const val RedisHashMapKeyIncreaseRates = "gatherRates"
    }
}
