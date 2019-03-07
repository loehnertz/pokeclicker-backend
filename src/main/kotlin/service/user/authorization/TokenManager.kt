package service.user.authorization

import io.ktor.http.Headers
import io.ktor.http.Parameters
import model.User
import model.Users
import model.getUser
import org.mindrot.jbcrypt.BCrypt
import redis.clients.jedis.Jedis
import java.util.concurrent.TimeUnit

object TokenManager {
    private const val AuthorizationHttpRequestHeaderKey = "Authorization"
    private const val AuthorizationHttpRequestHeaderTokenKey = "Token: "
    private const val AuthorizationHttpRequestParameterKey = "token"
    private const val RedisBaseKeyTokenByUsername = "token_by_username"
    private const val RedisBaseKeyUsernameByToken = "username_by_token"

    private val redis = Jedis(System.getenv("redis_host"))

    internal val UserTokenExpiryInSeconds = TimeUnit.DAYS.toSeconds(7).toInt()

    fun createToken(username: String): String {
        val token = generateToken()
        val redisTokenByUsernameKey = generateRedisTokenByUsernameKey(username)
        val redisUsernameByTokenKey = generateRedisUsernameByTokenKey(token)

        insertToken(
            username = username,
            token = token,
            redisUsernameByTokenKey = redisUsernameByTokenKey,
            redisTokenByUsernameKey = redisTokenByUsernameKey
        )

        return token
    }

    fun verifyTokenAndRetrieveUser(headers: Headers): User {
        val providedToken = retrieveTokenFromHttpRequestHeaders(headers)
        return verifyTokenAndRetrieveUser(providedToken)
    }

    fun verifyTokenAndRetrieveUser(parameters: Parameters): User {
        val providedToken = retrieveTokenFromHttpRequestParameters(parameters)
        return verifyTokenAndRetrieveUser(providedToken)
    }

    private fun verifyTokenAndRetrieveUser(providedToken: String): User {
        val username = retrieveUsernameByToken(providedToken) ?: throw TokenExpiredException()
        val tokenIsValid = verifyToken(username = username, providedToken = providedToken)

        if (!tokenIsValid) {
            throw TokenExpiredException()
        } else {
            return Users.getUser(username)
        }
    }

    private fun retrieveTokenFromHttpRequestHeaders(headers: Headers): String {
        val tokenHeader = headers[AuthorizationHttpRequestHeaderKey] ?: throw TokenMissingException()
        return tokenHeader.removePrefix(AuthorizationHttpRequestHeaderTokenKey)
    }

    private fun retrieveTokenFromHttpRequestParameters(parameters: Parameters): String {
        return parameters[AuthorizationHttpRequestParameterKey] ?: throw TokenMissingException()
    }

    private fun insertToken(username: String, token: String, redisUsernameByTokenKey: String, redisTokenByUsernameKey: String) {
        redis.del(redisTokenByUsernameKey)
        redis.del(redisUsernameByTokenKey)

        redis.set(redisUsernameByTokenKey, username)
        redis.set(redisTokenByUsernameKey, token)

        redis.expire(redisUsernameByTokenKey, UserTokenExpiryInSeconds)
        redis.expire(redisTokenByUsernameKey, UserTokenExpiryInSeconds)
    }

    private fun verifyToken(username: String, providedToken: String): Boolean {
        val redisTokenByUsernameKey = generateRedisTokenByUsernameKey(username)
        val actualToken = redis.get(redisTokenByUsernameKey)

        return !(actualToken == null || providedToken != actualToken)
    }

    private fun retrieveUsernameByToken(token: String): String? {
        val redisUsernameByTokenKey = generateRedisUsernameByTokenKey(token)
        return redis.get(redisUsernameByTokenKey)
    }

    private fun generateToken(): String {
        return BCrypt.gensalt()
    }

    private fun generateRedisTokenByUsernameKey(username: String): String {
        return "$RedisBaseKeyTokenByUsername:$username"
    }

    private fun generateRedisUsernameByTokenKey(token: String): String {
        return "$RedisBaseKeyUsernameByToken:$token"
    }
}

class TokenMissingException(override val message: String = "Please provide a session token with the request") : Exception()

class TokenExpiredException(override val message: String = "Your session expired, please login again") : Exception()
