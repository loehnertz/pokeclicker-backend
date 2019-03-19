package utility

class RedisConnector {
    fun hmget(key: String, field: String): List<String> {
        RedisFactory.retrieveRedisReadingClient().use { redis ->
            return redis.hmget(key, field)
        }
    }

    fun hgetAll(key: String): MutableMap<String, String>? {
        RedisFactory.retrieveRedisReadingClient().use { redis ->
            return redis.hgetAll(key)
        }
    }

    fun hmset(key: String, hash: Map<String, String>): String {
        RedisFactory.retrieveRedisWritingClient().use { redis ->
            return redis.hmset(key, hash)
        }
    }

    fun hincrBy(key: String, field: String, value: Long): Long {
        RedisFactory.retrieveRedisWritingClient().use { redis ->
            return redis.hincrBy(key, field, value)
        }
    }

    fun hincrByFloat(key: String, field: String, value: Double): Double {
        RedisFactory.retrieveRedisWritingClient().use { redis ->
            return redis.hincrByFloat(key, field, value)
        }
    }

    fun smembers(key: String): Set<String> {
        RedisFactory.retrieveRedisReadingClient().use { redis ->
            return redis.smembers(key)
        }
    }

    fun sadd(key: String, vararg members: String): Long {
        RedisFactory.retrieveRedisWritingClient().use { redis ->
            return redis.sadd(key, *members)
        }
    }
}
