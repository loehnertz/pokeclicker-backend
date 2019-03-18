package service.user.balance

import model.User
import model.Users
import model.getPokemons
import utility.RedisConnector

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

    fun updateIncreaseRate(increaseRateAddition: Long) {
        RedisConnector().hincrBy(RedisHashMapKeyIncreaseRates, user.name, increaseRateAddition)
    }

    fun retrieveIncreaseRate(): Long? {
        return try {
            RedisConnector().hmget(RedisHashMapKeyIncreaseRates, user.name).firstOrNull()?.toLong()
        } catch (exception: Exception) {
            // TODO: Add logging here
            null
        }
    }

    private fun calculateIncreaseRatePerSecond(): Long {
        val pokemons = Users.getPokemons(user.id)
        return Math.ceil(pokemons.fold(0L) { sum, pokemon -> sum + pokemon.xp }.toDouble() / IncreaseRateScalingFactor).toLong()
    }

    companion object {
        const val IncreaseRateScalingFactor = 20
        private const val RedisHashMapKeyIncreaseRates = "gatherRates"
    }
}
