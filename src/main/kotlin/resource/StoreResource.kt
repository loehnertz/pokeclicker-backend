package resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import main.Session
import service.StoreService

fun Route.store(storeService: StoreService) {
    route("/store") {
        route("/boosterpacks") {
            get("/") {
                call.respond(storeService.getAllBoosterpacks())
            }

            get("/{id}") {
                val boosterpackId = call.parameters["id"]

                if (boosterpackId != null) {
                    call.respond(storeService.getSpecificBoosterpack(boosterpackId.toInt()))
                } else {
                    call.respond("No ID was specified")
                }
            }

            get("/buy/{id}") {
                val boosterpackId = call.parameters["id"]

                val session: Session? = call.sessions.get<Session>()

                if (boosterpackId != null) {
                    try {
                        call.respond(storeService.buyBoosterpack(boosterpackId.toInt(), 2))
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.BadRequest, e.localizedMessage)
                    }
                } else {
                    call.respond("No ID was specified")
                }
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val mapper = jacksonObjectMapper().apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }
}
