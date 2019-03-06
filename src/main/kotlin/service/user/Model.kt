package service.user

import service.user.TokenManager.UserTokenExpiryInSeconds

data class UserLoginRequest(
    val username: String,
    val password: String
)

data class UserRegistrationRequest(
    val username: String,
    val email: String,
    val password: String
)

data class UserAuthenticationResponse(
    val ok: Boolean = false,
    val error: String? = null,
    val token: String? = null,
    val tokenExpiryInSeconds: Int = UserTokenExpiryInSeconds
)
