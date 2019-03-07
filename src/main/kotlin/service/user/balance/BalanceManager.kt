package service.user.balance

import kotlinx.coroutines.delay
import model.User
import model.Users
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import utility.RedisFactory
import utility.Scheduler
import java.util.concurrent.TimeUnit

class BalanceManager(val user: User) {
    fun increaseCurrentBalance(increaseAmount: Long) {
        val redis = RedisFactory.retrieveRedisClient()
        redis.hincrBy(RedisKeyUserBalances, user.name, increaseAmount)
        redis.close()
    }

    fun incrementCurrentBalance() {
        increaseCurrentBalance(1)
    }

    fun retrieveCurrentBalance(): Long {
        val redis = RedisFactory.retrieveRedisClient()

        val currentBalance = redis.hmget(RedisKeyUserBalances, user.name).firstOrNull()?.toLong()

        redis.close()

        return if (currentBalance != null) {
            currentBalance
        } else {
            setCurrentBalance()
            user.pokeDollars
        }
    }

    private fun setCurrentBalance() {
        val redis = RedisFactory.retrieveRedisClient()

        redis.hmset(RedisKeyUserBalances, mapOf(user.name to user.pokeDollars.toString()))

        redis.close()
    }

    companion object {
        private const val RedisKeyUserBalances = "balances"

        suspend fun syncAllCurrentBalancesToDatabase() {
            while (true) {
                val redis = RedisFactory.retrieveRedisClient()

                val allBalances = redis.hgetAll(RedisKeyUserBalances)

                redis.close()

                for ((username, currentBalance) in allBalances) {
                    transaction {
                        Users.update({ Users.name eq username }) {
                            it[pokeDollars] = currentBalance.toLong()
                        }
                    }
                }

                delay(TimeUnit.MINUTES.toMillis(Scheduler.BalanceSyncTimeoutInMinutes))
            }
        }
    }
}
