package resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.mapNotNull
import kotlinx.coroutines.delay
import model.Users
import model.getUser
import service.user.UserService
import service.user.authorization.TokenExpiredException
import service.user.authorization.TokenManager
import service.user.authorization.TokenMissingException
import service.user.balance.BalanceIncreaseRateManager
import service.user.data.UserLoginRequest
import service.user.data.UserRegistrationRequest
import service.user.session.SessionLockAlreadyAcquired
import service.user.session.SessionSemaphore
import utility.Scheduler
import java.util.concurrent.TimeUnit

const val WebSocketResponseBalanceKeyword = "balance"
const val WebSocketResponseGatherRateKeyword = "rate"
const val WebSocketResponseLeaderboardKeyword = "leaderboard"
const val WebSocketClickingKeyword = "click"
const val WebSocketClosingKeyword = "bye"
const val WebSocketClickingMessage = "Click successfully received"
const val WebSocketClosedByClientMessage = "Connection closed by client"
const val WebSocketClosedByExceptionMessage = "An exception occurred, please reopen the socket"
const val WebSocketUnknownCommandMessage = "The received command is unknown"

fun Route.user(userService: UserService) {
    route("/users") {
        post("/login") {
            try {
                val loginRequest = call.receive<UserLoginRequest>()
                val loginResponse = userService.loginUser(loginRequest)
                call.respond(loginResponse)
            } catch (exception: MissingKotlinParameterException) {
                call.respond(HttpStatusCode.BadRequest, "You are missing one or multiple parameters")
            }
        }

        post("/register") {
            try {
                val registrationRequest = call.receive<UserRegistrationRequest>()
                val registrationResponse = userService.registerUser(registrationRequest)
                call.respond(registrationResponse)
            } catch (exception: MissingKotlinParameterException) {
                call.respond(HttpStatusCode.BadRequest, "You are missing one or multiple parameters")
            }
        }

        get("/") {
            try {
                val user = TokenManager.verifyTokenAndRetrieveUser(call.request.headers)

                call.respond(Users.getUser(user.id))
            } catch (exception: Exception) {
                when (exception) {
                    is TokenExpiredException -> call.respond(HttpStatusCode.Unauthorized, exception.message)
                    is TokenMissingException -> call.respond(HttpStatusCode.Unauthorized, exception.message)
                    else -> throw exception
                }
            }
        }

        route("/pokemon") {
            get("/") {
                try {
                    val user = TokenManager.verifyTokenAndRetrieveUser(call.request.headers)

                    call.respond(userService.getUserPokemon(user.id))
                } catch (exception: Exception) {
                    when (exception) {
                        is TokenExpiredException -> call.respond(HttpStatusCode.Unauthorized, exception.message)
                        is TokenMissingException -> call.respond(HttpStatusCode.Unauthorized, exception.message)
                        else -> throw exception
                    }
                }
            }

            post("/merge") {
                try {
                    val user = TokenManager.verifyTokenAndRetrieveUser(call.request.headers)

                    call.respond(userService.mergeUserPokemon(user, call.receive()))
                } catch (exception: Exception) {
                    when (exception) {
                        is TokenExpiredException -> call.respond(HttpStatusCode.Unauthorized, exception.message)
                        is TokenMissingException -> call.respond(HttpStatusCode.Unauthorized, exception.message)
                        else -> call.respond(HttpStatusCode.BadRequest, exception.localizedMessage)
                    }
                }
            }
        }

        webSocket("/balance") {
            try {
                val user = TokenManager.verifyTokenAndRetrieveUser(call.parameters)
                val balanceManager = userService.buildBalanceManager(user)
                val balanceIncreaseRateManager = BalanceIncreaseRateManager(user)

                SessionSemaphore.acquireBalanceSession(user)

                try {
                    while (true) {
                        balanceIncreaseRateManager.increaseBalanceBasedOnIncreaseRate(balanceManager)

                        val currentBalance = balanceManager.retrieveCurrentBalance()
                        val currentIncreaseRate = balanceIncreaseRateManager.retrieveIncreaseRate()
                        val leaderboardAsJson = userService.retrieveLeaderboardAsJson()

                        outgoing.send(Frame.Text("$WebSocketResponseBalanceKeyword:$currentBalance"))
                        outgoing.send(Frame.Text("$WebSocketResponseGatherRateKeyword:$currentIncreaseRate"))
                        outgoing.send(Frame.Text("$WebSocketResponseLeaderboardKeyword:$leaderboardAsJson"))

                        delay(TimeUnit.SECONDS.toMillis(Scheduler.BalanceIncreaseTimeoutInSeconds))
                    }
                } catch (exception: Exception) {
                    close(CloseReason(CloseReason.Codes.UNEXPECTED_CONDITION, message = WebSocketClosedByExceptionMessage))
                } finally {
                    balanceManager.syncCurrentBalanceToDatabase()
                    SessionSemaphore.releaseBalanceSession(user)
                }
            } catch (exception: Exception) {
                when (exception) {
                    is TokenExpiredException -> close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, message = exception.message))
                    is TokenMissingException -> close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, message = exception.message))
                    is SessionLockAlreadyAcquired -> close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, message = exception.message))
                    else -> throw exception
                }
            }
        }

        webSocket("/clicking") {
            try {
                val user = TokenManager.verifyTokenAndRetrieveUser(call.parameters)
                val balanceManager = userService.buildBalanceManager(user)

                SessionSemaphore.acquireClickingSession(user)

                try {
                    incoming.mapNotNull { it as? Frame.Text }.consumeEach { frame ->
                        val text = frame.readText()

                        when {
                            text.equals(WebSocketClickingKeyword, ignoreCase = true) -> {
                                balanceManager.increaseCurrentBalance()
                                outgoing.send(Frame.Text(WebSocketClickingMessage))
                            }
                            text.equals(WebSocketClosingKeyword, ignoreCase = true) -> {
                                outgoing.send(Frame.Text(WebSocketClosedByClientMessage))
                                close(CloseReason(CloseReason.Codes.NORMAL, message = WebSocketClosedByClientMessage))
                            }
                            else -> outgoing.send(Frame.Text(WebSocketUnknownCommandMessage))
                        }
                    }
                } catch (exception: Exception) {
                    close(CloseReason(CloseReason.Codes.UNEXPECTED_CONDITION, message = WebSocketClosedByExceptionMessage))
                } finally {
                    SessionSemaphore.releaseClickingSession(user)
                }
            } catch (exception: Exception) {
                when (exception) {
                    is TokenExpiredException -> close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, message = exception.message))
                    is TokenMissingException -> close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, message = exception.message))
                    is SessionLockAlreadyAcquired -> close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, message = exception.message))
                    else -> throw exception
                }
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val mapper = jacksonObjectMapper().apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }
}
