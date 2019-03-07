package service.user.balance

import model.User
import model.Users
import model.toUser
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import utility.RedisFactory
import utility.Scheduler

class BalanceIncreaser(val user: User) {
    fun increaseBalanceBasedOnIncreaseRate() {
        val currentIncreaseRatePerMinute = retrieveIncreaseRate()
        if (currentIncreaseRatePerMinute != null) {
            val balanceIncreaseSinceLastTick = calculateBalanceIncreaseSinceLastTick(currentIncreaseRatePerMinute)
            BalanceManager(user).increaseCurrentBalance(balanceIncreaseSinceLastTick)
        }
    }

    private fun calculateBalanceIncreaseSinceLastTick(currentIncreaseRatePerMinute: Long): Long {
        return currentIncreaseRatePerMinute * Scheduler.BalanceIncreaseTimeoutInMinutes
    }

    fun updateIncreaseRate() {
        val increaseRatePerMinute = calculateIncreaseRatePerMinute()
        redis.hmset(RedisHashMapKeyIncreaseRates, mapOf(user.name to increaseRatePerMinute.toString()))
    }

    private fun retrieveIncreaseRate(): Long? {
        return redis.hmget(RedisHashMapKeyIncreaseRates, user.name).firstOrNull()?.toLong()
    }

    private fun calculateIncreaseRatePerMinute(): Long {
        return 60  // TODO: Implement this
    }

    companion object {
        private const val RedisHashMapKeyIncreaseRates = "gather_rates"

        private val redis = RedisFactory.getRedisClient()

        fun increaseBalanceBasedOnIncreaseRateOfEveryUser() {
            retrieveAllUsers().forEach { BalanceIncreaser(it).increaseBalanceBasedOnIncreaseRate() }
        }

        fun updateIncreaseRateOfEveryUser() {
            retrieveAllUsers().forEach { BalanceIncreaser(it).updateIncreaseRate() }
        }

        private fun retrieveAllUsers(): List<User> {
            return transaction { Users.selectAll().map { Users.toUser(it) } }
        }
    }
}
