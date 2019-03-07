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
    suspend fun increaseBalanceBasedOnIncreaseRate() {
        val balanceManager = BalanceManager(user)
        while (true) {
            val currentIncreaseRatePerMinute = retrieveIncreaseRate()
            if (currentIncreaseRatePerMinute != null) balanceManager.increaseCurrentBalance(currentIncreaseRatePerMinute)
            delay(TimeUnit.SECONDS.toMillis(Scheduler.BalanceIncreaseTimeoutInSeconds))
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
            retrieveAllUsers().forEach { GlobalScope.launch { BalanceIncreaser(it).increaseBalanceBasedOnIncreaseRate() } }
        }

        suspend fun updateIncreaseRateOfEveryUser() {
            while (true) {
                retrieveAllUsers().forEach { BalanceIncreaser(it).updateIncreaseRate() }
                delay(TimeUnit.SECONDS.toMillis(Scheduler.BalanceIncreaseSyncTimeoutInSeconds))
            }
        }

        private fun retrieveAllUsers(): List<User> {
            return transaction { Users.selectAll().map { Users.toUser(it) } }
        }
    }
}
