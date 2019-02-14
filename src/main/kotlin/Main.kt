import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.jackson.jackson
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import resource.user
import service.UserService

import PokeApiAdapter
import me.sargunvohra.lib.pokekotlin.client.PokeApiClient
import me.sargunvohra.lib.pokekotlin.model.Pokemon
import me.sargunvohra.lib.pokekotlin.model.PokemonSpecies


fun Application.module() {
    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets)

    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
        }
    }

    // DatabaseFactory.init()

    val userService = UserService()

    install(Routing) {
        user(userService)
    }
}

fun getRandomPkmn(): String {
    val pokeApi = PokeApiClient()
    val randInt = (1..807).random()
    return pokeApi.getPokemonSpecies(randInt).name
}

fun main() {
    embeddedServer(Netty, 8080, watchPaths = listOf("MainKt"), module = Application::module).start()

    val pkmn = getRandomPkmn()
    println("Random Pokemon:$pkmn")

    val pkmnInfo = PokeApiAdapter().getPkmnData(1)
    println("First Pokemon in database::${pkmnInfo.apiInfo.name}")
}
