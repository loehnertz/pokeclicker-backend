package service.user

import model.User
import model.Users
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import redis.clients.jedis.Jedis

class BalanceManager(val user: User) {
    fun increaseCurrentBalance(increaseAmount: Long = 1): Long? {
        return redis.hincrBy(RedisKeyUserBalances, user.name, increaseAmount)
    }

    fun retrieveCurrentBalance(): Long? {
        return redis.hmget(RedisKeyUserBalances, user.name).firstOrNull()?.toLong()
    }

    companion object {
        private const val RedisKeyUserBalances = "balances"

        private val redis = Jedis(System.getenv("redis_host"))

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
