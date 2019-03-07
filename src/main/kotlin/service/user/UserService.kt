package service.user

import model.Pokemons
import model.User
import model.toPokemon
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import service.user.authentication.Login
import service.user.authentication.Registration
import service.user.balance.BalanceManager
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

    fun getUserPokemon(userId: Int): List<model.Pokemon> {
        return transaction { Pokemons.select { Pokemons.owner eq userId }.map { Pokemons.toPokemon(it) } }
    }

    fun buildBalanceManager(user: User): BalanceManager {
        return BalanceManager(user)
    }
}
