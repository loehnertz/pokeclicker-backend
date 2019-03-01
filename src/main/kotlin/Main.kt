package main

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.oauth
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.origin
import io.ktor.jackson.jackson
import io.ktor.request.host
import io.ktor.request.port
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.SessionTransportTransformerMessageAuthentication
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import io.ktor.util.hex
import io.ktor.websocket.WebSockets
import resource.store
import resource.user
import service.StoreService
import service.UserService
import service.googleOauthProvider
import utility.DatabaseFactory

class Session(val userId: String)

fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets)

    install(Sessions) {
        cookie<Session>("oauthSampleSessionId") {
            val secretSignKey = hex("000102030405060708090a0b0c0d0e0f") // @TODO: Remember to change this!
            //val secretSignKey = hex(System.getenv("pokeclicker_session_key"))
            transform(SessionTransportTransformerMessageAuthentication(secretSignKey))
        }
    }

    install(Authentication) {
        oauth("google-oauth") {
            client = HttpClient(Apache)
            providerLookup = { googleOauthProvider }
            urlProvider = { redirectUrl("/login") }
        }
    }

    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
        }
    }

    DatabaseFactory.init()

    install(Routing) {
        user(UserService())    
        store(StoreService())
    }
}

private fun ApplicationCall.redirectUrl(path: String): String {
    val defaultPort = if (request.origin.scheme == "http") 80 else 443
    val hostPort = request.host() + request.port().let { port -> if (port == defaultPort) "" else ":$port" }
    val protocol = request.origin.scheme
    return "$protocol://$hostPort$path"
}

fun main() {
    embeddedServer(Netty, 8080, watchPaths = listOf("MainKt"), module = Application::module).start()
}
