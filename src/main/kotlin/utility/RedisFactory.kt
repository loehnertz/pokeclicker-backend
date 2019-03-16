package utility

import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

object RedisFactory {
    private val redisMasterClientPool = JedisPool(JedisPoolConfig(), System.getenv("redis-master_host"))
    private val redisSlaveClientPool = JedisPool(JedisPoolConfig(), System.getenv("redis-slave_host"))

    fun retrieveRedisWritingClient(): Jedis {
        return redisMasterClientPool.resource
    }

    fun retrieveRedisReadingClient(): Jedis {
        return redisSlaveClientPool.resource
    }
}
