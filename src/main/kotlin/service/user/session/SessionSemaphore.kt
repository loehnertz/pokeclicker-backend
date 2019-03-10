package service.user.session

import model.User
import utility.RedisFactory

object SessionSemaphore {
    private const val RedisLockValue = "acquired"
    private const val RedisLockBaseKeyBalance = "balance_session"
    private const val RedisLockBaseKeyClicking = "clicking_session"
    private const val RedisResponseKeyDidAlreadyExist: Long = 0

    fun acquireBalanceSession(user: User) {
        RedisFactory.retrieveRedisClient().use { redis ->
            val successfullyAcquired = redis.setnx(generateRedisKey(RedisLockBaseKeyBalance, user), RedisLockValue)
            if (successfullyAcquired == RedisResponseKeyDidAlreadyExist) throw SessionLockAlreadyAcquired()
        }
    }

    fun acquireClickingSession(user: User) {
        RedisFactory.retrieveRedisClient().use { redis ->
            val successfullyAcquired = redis.setnx(generateRedisKey(RedisLockBaseKeyClicking, user), RedisLockValue)
            if (successfullyAcquired == RedisResponseKeyDidAlreadyExist) throw SessionLockAlreadyAcquired()
        }
    }

    fun releaseBalanceSession(user: User) {
        RedisFactory.retrieveRedisClient().use { redis ->
            redis.del(generateRedisKey(RedisLockBaseKeyBalance, user))
        }
    }

    fun releaseClickingSession(user: User) {
        RedisFactory.retrieveRedisClient().use { redis ->
            redis.del(generateRedisKey(RedisLockBaseKeyClicking, user))
        }
    }

    fun releaseAllSessions(user: User) {
        releaseBalanceSession(user)
        releaseClickingSession(user)
    }

    private fun generateRedisKey(baseKey: String, user: User): String {
        return "$baseKey:${user.name}"
    }
}

class SessionLockAlreadyAcquired(override val message: String = "You already have another open session") : Exception()
