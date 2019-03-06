package service.user

import model.User
import redis.clients.jedis.Jedis

class BalanceManager(val user: User) {
    private val redis = Jedis(System.getenv("redis_host"))

    fun retrieveCurrentBalance() {

    }
}
