package service.user.balance

import model.User
import model.Users
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import utility.RedisFactory

class BalanceManager(val user: User) {
    fun increaseCurrentBalance(increaseAmount: Long = 1) {
        val redis = RedisFactory.retrieveRedisClient()
        redis.hincrBy(RedisKeyUserBalances, user.name, increaseAmount)
        redis.close()
    }

    fun decreaseCurrentBalance(decreaseAmount: Long = 1) {
        val redis = RedisFactory.retrieveRedisClient()
        redis.hincrBy(RedisKeyUserBalances, user.name, (decreaseAmount * -1))
        redis.close()
    }

    fun retrieveCurrentBalance(): Long {
        val redis = RedisFactory.retrieveRedisClient()

        val currentBalance = redis.hmget(RedisKeyUserBalances, user.name).firstOrNull()?.toLong()

        redis.close()

        return if (currentBalance != null) {
            currentBalance
        } else {
            setCurrentBalance(user.pokeDollars)
            user.pokeDollars
        }
    }

    fun syncCurrentBalanceToDatabase() {
        transaction {
            Users.update({ Users.name eq user.name }) {
                it[pokeDollars] = retrieveCurrentBalance()
            }
        }
    }

    private fun setCurrentBalance(value: Long) {
        val redis = RedisFactory.retrieveRedisClient()

        redis.hmset(RedisKeyUserBalances, mapOf(user.name to value.toString()))

        redis.close()
    }

    companion object {
        private const val RedisKeyUserBalances = "balances"
    }
}
