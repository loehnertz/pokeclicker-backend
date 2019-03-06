package service.user

import model.Users
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

object Registration {
    fun registerUser(registrationRequest: UserRegistrationRequest): UserAuthenticationResponse {
        return if (userAlreadyExists(registrationRequest.username)) {
            UserAuthenticationResponse(error = "A user with your chosen name already exists")
        } else if (registrationRequest.password.length < 6) {
            UserAuthenticationResponse(error = "Password should be at least 6 characters long")
        } else if (registrationRequest.username.length < 4) {
            UserAuthenticationResponse(error = "Username should be at least 4 characters long")
        } else if (!usernameIsValid(registrationRequest.username)) {
            UserAuthenticationResponse(error = "Login should be consists of digits, letters, dots or underscores")
        } else {
            createUserAccount(registrationRequest)
        }
    }

    private fun createUserAccount(registrationRequest: UserRegistrationRequest): UserAuthenticationResponse {
        val hashedPassword = BCrypt.hashpw(registrationRequest.password, BCrypt.gensalt())

        return try {
            transaction {
                Users.insert {
                    it[name] = registrationRequest.username
                    it[email] = registrationRequest.email
                    it[password] = hashedPassword
                }
            }

            val userToken = TokenManager.createToken(registrationRequest.username)

            UserAuthenticationResponse(ok = true, token = userToken)
        } catch (exception: Exception) {
            UserAuthenticationResponse(error = "An unidentified error occurred during the account creation")
        }
    }

    private fun userAlreadyExists(username: String): Boolean {
        val userAlreadyExists = transaction { Users.select { Users.name eq username }.firstOrNull() }
        return userAlreadyExists != null
    }

    private fun usernameIsValid(username: String): Boolean {
        return Regex("^[\\pL\\p{Mn}\\p{Nd}\\p{Pc}]+$").matches(username)
    }
}
