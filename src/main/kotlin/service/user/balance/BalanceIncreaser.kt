package service.user.balance

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.User
import model.Users
import model.toUser
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import utility.RedisFactory
import utility.Scheduler
import java.util.concurrent.TimeUnit

class BalanceIncreaser(val user: User) {
    fun increaseBalanceBasedOnIncreaseRate() {
        val currentIncreaseRatePerMinute = retrieveIncreaseRate()
        if (currentIncreaseRatePerMinute != null) {
            val balanceIncreaseSinceLastTick = calculateBalanceIncreaseSinceLastTick(currentIncreaseRatePerMinute)
            BalanceManager(user).increaseCurrentBalance(balanceIncreaseSinceLastTick)
        }
    }

    fun updateIncreaseRate() {
        val redis = RedisFactory.retrieveRedisClient()

        val increaseRatePerMinute = calculateIncreaseRatePerSecond()
        redis.hmset(RedisHashMapKeyIncreaseRates, mapOf(user.name to increaseRatePerMinute.toString()))

        redis.close()
    }

    private fun retrieveIncreaseRate(): Long? {
        val redis = RedisFactory.retrieveRedisClient()

        val increaseRate = redis.hmget(RedisHashMapKeyIncreaseRates, user.name).firstOrNull()

        redis.close()

        return increaseRate?.toLong()
    }

    private fun calculateIncreaseRatePerSecond(): Long {
        return 60  // TODO: Implement this
    }

    companion object {
        private const val RedisHashMapKeyIncreaseRates = "gather_rates"

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
