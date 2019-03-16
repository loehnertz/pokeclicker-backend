package utility

import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

object RedisFactory {
    private val redisMasterClientPool = JedisPool(JedisPoolConfig(), System.getenv("redis_master_host"))
    private val redisSlaveClientPool = JedisPool(JedisPoolConfig(), System.getenv("redis_slave_host"))

    fun retrieveRedisWritingClient(): Jedis {
        return redisMasterClientPool.resource
    }

    fun retrieveRedisReadingClient(): Jedis {
        return redisSlaveClientPool.resource
    }
}
