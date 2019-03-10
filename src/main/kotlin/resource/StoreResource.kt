package resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.features.BadRequestException
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import resource.data.Response
import service.store.StoreService
import service.user.authorization.TokenExpiredException
import service.user.authorization.TokenManager
import service.user.authorization.TokenMissingException
import utility.ErrorLogger

fun Route.store(storeService: StoreService) {
    route("/store") {
        route("/boosterpacks") {
            get("/") {
                try {
                    call.respond(storeService.getAllBoosterpacks())
                } catch (exception: Exception) {
                    ErrorLogger.logException(exception).also { throw exception }
                }
            }

            get("/{id}") {
                try {
                    val boosterpackId = call.parameters["id"]!!
                    val boosterpack = storeService.getSpecificBoosterpack(boosterpackId.toInt())

                    if (boosterpack != null) {
                        call.respond(boosterpack)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                } catch (exception: Exception) {
                    ErrorLogger.logException(exception).also { throw exception }
                }
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
                } catch (exception: BadRequestException) {
                    call.respond(HttpStatusCode.BadRequest, Response(error = exception.localizedMessage))
                } catch (exception: Exception) {
                    ErrorLogger.logException(exception).also { throw exception }
                }
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val mapper = jacksonObjectMapper().apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }
}
