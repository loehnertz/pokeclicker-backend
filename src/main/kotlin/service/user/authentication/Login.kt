package service.user.authentication

import model.Users
import model.getUser
import org.mindrot.jbcrypt.BCrypt
import service.user.authorization.TokenManager
import service.user.data.UserAuthenticationResponse
import service.user.data.UserLoginRequest
import service.user.session.SessionSemaphore

object Login {
    fun loginUser(userLoginRequest: UserLoginRequest): UserAuthenticationResponse {
        val hashedPassword = Users.getUser(userLoginRequest.username).password
        val passwordCorrect = BCrypt.checkpw(userLoginRequest.password, hashedPassword)

        return if (!passwordCorrect) {
            UserAuthenticationResponse(error = "The password you entered is incorrect")
        } else {
            val userToken = TokenManager.createToken(userLoginRequest.username)
            SessionSemaphore.releaseAllSessions(Users.getUser(userLoginRequest.username))
            UserAuthenticationResponse(ok = true, token = userToken)
        }
    }
}
