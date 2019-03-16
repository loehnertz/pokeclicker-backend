package service.user.balance

import model.User
import model.Users
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import utility.RedisConnector

class BalanceManager(val user: User) {
    fun increaseCurrentBalance(increaseAmount: Long = 1) {
        RedisConnector().hincrBy(RedisKeyUserBalances, user.name, increaseAmount)
    }

    fun decreaseCurrentBalance(decreaseAmount: Long = 1) {
        RedisConnector().hincrBy(RedisKeyUserBalances, user.name, (decreaseAmount * -1))
    }

    fun retrieveCurrentBalance(): Long {
        val currentBalance = RedisConnector().hmget(RedisKeyUserBalances, user.name).firstOrNull()?.toLong()

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
        RedisConnector().hmset(RedisKeyUserBalances, mapOf(user.name to value.toString()))
    }

    companion object {
        private const val RedisKeyUserBalances = "balances"
    }
}
