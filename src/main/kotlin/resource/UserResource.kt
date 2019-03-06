package resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.call
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import main.Session
import model.Users
import model.getUser
import service.UserService

fun Route.user(userService: UserService) {
    route("/users") {
        get("/") {
            val session = call.sessions.get<Session>()
            call.respondText("Hello, ${session?.userId}")
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

        authenticate("google-oauth") {
            route("/login") {
                handle {
                    val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
                        ?: error("No principal")

                    val json = HttpClient(Apache).get<String>("https://www.googleapis.com/userinfo/v2/me") {
                        header("Authorization", "Bearer ${principal.accessToken}")
                    }

                    val data = ObjectMapper().readValue<Map<String, Any?>>(json)
                    val id = data["id"] as String?

                    if (id != null) call.sessions.set(Session(id))
                    call.respondRedirect("/")
                }
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val mapper = jacksonObjectMapper().apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }
}
