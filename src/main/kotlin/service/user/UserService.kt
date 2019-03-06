package service.user

import model.User

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
