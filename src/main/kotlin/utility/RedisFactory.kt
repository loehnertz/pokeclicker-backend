package utility

import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

object RedisFactory {
    private val redisClientPool = JedisPool(JedisPoolConfig(), System.getenv("redis_host"))

    fun retrieveRedisClient(): Jedis {
        return redisClientPool.resource
    }
}
