package service.user

import model.Pokemon
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
import service.user.data.UserPokemonMergeRequest
import service.user.data.UserRegistrationRequest
import service.user.pokemon.PokemonMerger

class UserService {
    fun loginUser(loginRequest: UserLoginRequest): UserAuthenticationResponse {
        return Login.loginUser(loginRequest)
    }

    fun registerUser(registrationRequest: UserRegistrationRequest): UserAuthenticationResponse {
        return Registration.registerUser(registrationRequest)
    }

    fun getUserPokemon(userId: Int): List<Pokemon> {
        return transaction { Pokemons.select { Pokemons.owner eq userId }.map { Pokemons.toPokemon(it) } }
    }

    fun mergeUserPokemon(user: User, mergeRequest: UserPokemonMergeRequest): Pokemon {
        return PokemonMerger(user).mergePokemons(mergeRequest)
    }

    fun buildBalanceManager(user: User): BalanceManager {
        return BalanceManager(user)
    }
}
