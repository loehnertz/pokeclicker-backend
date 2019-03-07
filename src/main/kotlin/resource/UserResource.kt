package resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import model.Users
import model.getUser
import service.user.UserService
import service.user.data.UserLoginRequest
import service.user.data.UserRegistrationRequest

fun Route.user(userService: UserService) {
    route("/users") {
        post("/login") {
            try {
                val loginRequest = call.receive<UserLoginRequest>()
                val loginResponse = userService.loginUser(loginRequest)
                call.respond(loginResponse)
            } catch (exception: MissingKotlinParameterException) {
                call.respond("You are missing one or multiple parameters")
            }
        }

        post("/register") {
            try {
                val registrationRequest = call.receive<UserRegistrationRequest>()
                val registrationResponse = userService.registerUser(registrationRequest)
                call.respond(registrationResponse)
            } catch (exception: MissingKotlinParameterException) {
                call.respond("You are missing one or multiple parameters")
            }
        }

        get("/{id}") {
            val id = call.parameters["id"]
            if (id != null) {
                call.respond(Users.getUser(id.toInt()))
            }
        }

        get("/{id}/pokemon") {
            val id = call.parameters["id"]
            if (id != null) {
                call.respond(userService.getUserPokemon(id.toInt()))
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val mapper = jacksonObjectMapper().apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }
}
