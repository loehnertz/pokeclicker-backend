package utility

import redis.clients.jedis.Jedis

object RedisFactory {
    fun getRedisClient(): Jedis {
        return Jedis(System.getenv("redis_host"))
    }
}
