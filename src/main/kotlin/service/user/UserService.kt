package service.user

import model.Pokemons
import model.toPokemon
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
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

    fun getUserPokemon(userId: Int): List<model.Pokemon> {

        val ownedPokemons = transaction {
            model.Pokemons.select{Pokemons.owner eq userId}.map{model.Pokemons.toPokemon(it)}
        }

        return ownedPokemons
    }
}
