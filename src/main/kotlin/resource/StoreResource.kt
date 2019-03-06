package resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import service.store.StoreService
import service.user.TokenExpiredException
import service.user.TokenManager
import service.user.TokenMissingException

fun Route.store(storeService: StoreService) {
    route("/store") {
        route("/boosterpacks") {
            get("/") {
                call.respond(storeService.getAllBoosterpacks())
            }

            get("/{id}") {
                val boosterpackId = call.parameters["id"]!!
                call.respond(storeService.getSpecificBoosterpack(boosterpackId.toInt()))
            }

            get("/buy/{id}") {
                try {
                    val boosterpackId = call.parameters["id"]!!
                    val user = TokenManager.verifyTokenAndRetrieveUser(call.request.headers)
                    call.respond(storeService.buyBoosterpack(boosterpackId.toInt(), user))
                } catch (exception: TokenExpiredException) {
                    call.respond(exception.message)
                } catch (exception: TokenMissingException) {
                    call.respond(exception.message)
                } catch (exception: Exception) {
                    // TODO: Add logging here
                    throw exception
                }
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val mapper = jacksonObjectMapper().apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }
}
