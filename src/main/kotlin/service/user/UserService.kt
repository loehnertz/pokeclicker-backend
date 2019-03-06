package service.user

class UserService {
    fun loginUser(loginRequest: UserLoginRequest): UserAuthenticationResponse {
        return Login.loginUser(loginRequest)
    }

    fun registerUser(registrationRequest: UserRegistrationRequest): UserAuthenticationResponse {
        return Registration.registerUser(registrationRequest)
    }
}
