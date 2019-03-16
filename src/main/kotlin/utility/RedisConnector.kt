package utility

class RedisConnector {
    fun hmget(key: String, field: String): List<String> {
        RedisFactory.retrieveRedisReadingClient().use { redis ->
            return redis.hmget(key, field)
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
}
