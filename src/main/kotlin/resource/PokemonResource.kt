package resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import service.pokemon.PokemonService
import utility.PokeApi
import utility.PokeApiAdapter

fun Route.pokemon(pokemonService: PokemonService) {
    route("/pokemon") {
        get("/{id}") {
            val id = call.parameters["id"]!!
            call.respond(PokeApiAdapter().getPokemonData(id.toInt()))
        }

        get("/pokedex/{id}") {
            val id = call.parameters["id"]!!
            call.respond(PokeApi().getPokemon(id.toInt()))
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val mapper = jacksonObjectMapper().apply { setSerializationInclusion(JsonInclude.Include.NON_NULL) }
}
