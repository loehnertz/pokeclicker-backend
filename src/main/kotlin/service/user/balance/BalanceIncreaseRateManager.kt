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
        val increaseRatePerSecond = retrieveTotalExperiencePoints().divide(IncreaseRateScalingFactor.toBigDecimal(), CEILING)).setScale(0, CEILING)
        RedisConnector().hmset(RedisHashMapKeyIncreaseRates, mapOf(user.name to increaseRatePerSecond.toString()))
    }

    fun updateIncreaseRate(experiencePointsIncrease: BigDecimal) {
        val increaseRatePerSecondAddition = experiencePointsIncrease.divide(IncreaseRateScalingFactor.toBigDecimal(), CEILING).setScale(0, CEILING)
        RedisConnector().hincrByFloat(RedisHashMapKeyIncreaseRates, user.name, increaseRatePerSecondAddition.toDouble())
    }

    fun retrieveIncreaseRate(): BigDecimal? {
        return try {
            RedisConnector().hmget(RedisHashMapKeyIncreaseRates, user.name).firstOrNull()?.toBigDecimal()
        } catch (exception: Exception) {
            // TODO: Add logging here
            null
        }
    }

    private fun retrieveTotalExperiencePoints(): BigDecimal {
        val pokemons = Users.getPokemons(user.id)
        return pokemons.fold(BigDecimal(0)) { sum, pokemon -> sum.add(pokemon.xp) }
    }

    companion object {
        const val IncreaseRateScalingFactor = 20
        private const val RedisHashMapKeyIncreaseRates = "gatherRates"
    }
}
