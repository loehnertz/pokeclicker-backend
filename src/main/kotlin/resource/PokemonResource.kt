package resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.http.Parameters
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import service.PokemonService

fun Route.pokemon(pokemonService: PokemonService) {
    route("/pokemon") {
        get("/{id}") {
            val id = call.parameters["id"]
            if (id != null) {
//                val postParameters: Parameters = call.receiveParameters()
                call.respond(pokemonService.getDBPokemon(id.toInt())) //, postParameters["id"]!!.toInt()))
            }
        }

        get("/pokedex/{id}") {
            val id = call.parameters["id"]
            if (id != null) {
//                val postParameters: Parameters = call.receiveParameters()
                call.respond(pokemonService.getPokedexPokemon(id.toInt())) //, postParameters["id"]!!.toInt()))
            }
        }
    }

    val mapper = jacksonObjectMapper().apply {
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }
}

//package resource
//
//import com.fasterxml.jackson.annotation.JsonInclude
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import io.ktor.application.call
//import io.ktor.response.respond
//import io.ktor.routing.Route
//import io.ktor.routing.get
//import io.ktor.routing.route
//import service.StoreService
//
//fun Route.store(storeService: StoreService) {
//    route("/store") {
//        get("/boosterpacks") {
//            call.respond(storeService.getAllBoosterpacks())
//        }
//    }
//
//    @Suppress("UNUSED_VARIABLE")
//    val mapper = jacksonObjectMapper().apply {
//        setSerializationInclusion(JsonInclude.Include.NON_NULL)
//    }
//}
