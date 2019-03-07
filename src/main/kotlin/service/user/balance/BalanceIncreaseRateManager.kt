package service.user.balance

import model.User
import model.Users
import model.getPokemons
import utility.RedisFactory

class BalanceIncreaseRateManager(val user: User) {
    fun increaseBalanceBasedOnIncreaseRate(balanceManager: BalanceManager) {
        val currentIncreaseRatePerMinute = retrieveIncreaseRate()
        if (currentIncreaseRatePerMinute != null) balanceManager.increaseCurrentBalance(currentIncreaseRatePerMinute)
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

    private fun retrieveIncreaseRate(): Long? {
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
        return pokemons.fold(0) { sum, pokemon -> sum + pokemon.apiInfo!!.baseExperience }.toLong()
    }

    companion object {
        private const val RedisHashMapKeyIncreaseRates = "gatherRates"
    }
}
