package resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import main.Session
import service.user.UserLoginRequest
import service.user.UserRegistrationRequest
import service.user.UserService

fun Route.user(userService: UserService) {
    route("/users") {
        get("/") {
            val session = call.sessions.get<Session>()
            call.respondText("Hello, ${session?.userId}")
        }

        post("/login") {
            try {
                val loginRequest = call.receive<UserLoginRequest>()
                val loginResponse = userService.loginUser(loginRequest)
                call.respond(loginResponse)
            } catch (exception: MissingKotlinParameterException) {
                call.respond("You are missing one or multiple parameters")
            }
        }

                    if (id != null) call.sessions.set(Session(id))
                    call.respondRedirect("/")
                }
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val mapper = jacksonObjectMapper().apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }
}
