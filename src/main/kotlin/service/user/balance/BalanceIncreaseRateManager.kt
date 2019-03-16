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
        return pokemons.fold(0) { sum, pokemon -> sum + pokemon.thinApiInfo!!.xp }.toLong()
    }

    companion object {
        private const val RedisHashMapKeyIncreaseRates = "gatherRates"
    }
}
