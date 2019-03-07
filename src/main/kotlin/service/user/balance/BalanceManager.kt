package service.user.balance

import model.User
import model.Users
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import utility.RedisFactory

class BalanceManager(val user: User) {
    fun increaseCurrentBalance(increaseAmount: Long): Long? {
        return redis.hincrBy(RedisKeyUserBalances, user.name, increaseAmount)
    }

    fun incrementCurrentBalance(): Long? {
        return increaseCurrentBalance(1)
    }

    fun retrieveCurrentBalance(): Long {
        val currentBalance = redis.hmget(RedisKeyUserBalances, user.name).firstOrNull()?.toLong()

        return if (currentBalance != null) {
            currentBalance
        } else {
            setCurrentBalance()
            user.pokeDollars
        }
    }

    private fun setCurrentBalance(): String? {
        return redis.hmset(RedisKeyUserBalances, mapOf(user.name to user.pokeDollars.toString()))
    }

    companion object {
        private const val RedisKeyUserBalances = "balances"

        private val redis = RedisFactory.getRedisClient()

        fun syncAllCurrentBalancesToDatabase() {
            val allBalances = redis.hgetAll(RedisKeyUserBalances)

            for ((username, currentBalance) in allBalances) {
                transaction {
                    Users.update({ Users.name eq username }) {
                        it[pokeDollars] = currentBalance.toLong()
                    }
                }
            }
        }
    }
}