package main

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import resource.pokemon
import resource.store
import resource.user
import service.pokemon.PokemonService
import service.store.StoreService
import service.user.UserService
import utility.DatabaseFactory
import utility.Scheduler

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
        pokemon(PokemonService())
    }
}

fun main() {
    embeddedServer(
        factory = Netty,
        port = System.getenv("backend_port").toInt(),
        watchPaths = listOf("MainKt"),
        module = Application::module
    ).start()
}
