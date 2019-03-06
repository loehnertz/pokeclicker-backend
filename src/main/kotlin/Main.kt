package main

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
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
import service.store.StoreService
import service.user.UserService
import utility.DatabaseFactory
import utility.googleOauthProvider

class Session(val userId: String)

fun Application.module() {
    install(DefaultHeaders)

    install(CallLogging)

    install(WebSockets)

    install(CORS) {
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        header(HttpHeaders.AccessControlAllowOrigin)
        anyHost()
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

fun main() {
    embeddedServer(Netty, 8080, watchPaths = listOf("MainKt"), module = Application::module).start()
}
