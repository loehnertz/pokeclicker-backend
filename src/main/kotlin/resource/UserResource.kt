package resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.mapNotNull
import kotlinx.coroutines.delay
import service.user.TokenManager
import service.user.UserLoginRequest
import service.user.UserRegistrationRequest
import service.user.UserService

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

        webSocket("/balance") {
            val user = TokenManager.verifyTokenAndRetrieveUser(call.parameters)
            val balanceManager = userService.buildBalanceManager(user)

            while (true) {
                val currentBalance = balanceManager.retrieveCurrentBalance()
                outgoing.send(Frame.Text(currentBalance.toString()))
                delay(1000)
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val mapper = jacksonObjectMapper().apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }
}
