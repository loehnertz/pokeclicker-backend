package service.user

import model.User

import service.user.authentication.Login
import service.user.authentication.Registration
import service.user.data.UserAuthenticationResponse
import service.user.data.UserLoginRequest
import service.user.data.UserRegistrationRequest

class UserService {
    fun loginUser(loginRequest: UserLoginRequest): UserAuthenticationResponse {
        return Login.loginUser(loginRequest)
    }

    fun registerUser(registrationRequest: UserRegistrationRequest): UserAuthenticationResponse {
        return Registration.registerUser(registrationRequest)
    }

    fun buildBalanceManager(user: User): BalanceManager {
        return BalanceManager(user)
    }
}
