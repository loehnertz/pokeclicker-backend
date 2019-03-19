package service.user.balance

import model.User
import model.Users
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import utility.RedisConnector
import java.math.BigDecimal

class BalanceManager(val user: User) {
    fun increaseCurrentBalance(increaseAmount: BigDecimal = BigDecimal(1)) {
        RedisConnector().hincrByFloat(RedisKeyUserBalances, user.name, increaseAmount.toDouble())
    }

    fun decreaseCurrentBalance(decreaseAmount: BigDecimal = BigDecimal(1)) {
        RedisConnector().hincrByFloat(RedisKeyUserBalances, user.name, (decreaseAmount.multiply(BigDecimal(-1))).toDouble())
    }

    fun retrieveCurrentBalance(): BigDecimal {
        val currentBalance = RedisConnector().hmget(RedisKeyUserBalances, user.name).firstOrNull()?.toBigDecimal()

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

    private fun setCurrentBalance(value: BigDecimal) {
        RedisConnector().hmset(RedisKeyUserBalances, mapOf(user.name to value.toString()))
    }

    companion object {
        const val RedisKeyUserBalances = "balances"
        private const val LeaderboardSize = 10

        fun retrieveLeaderboard(): Map<String, BigDecimal>? {
            return RedisConnector().hgetAll(RedisKeyUserBalances)
                ?.mapValues { it.value.toBigDecimal() }
                ?.toList()
                ?.sortedByDescending { it.second }
                ?.take(LeaderboardSize)
                ?.toMap()
        }
    }
}
