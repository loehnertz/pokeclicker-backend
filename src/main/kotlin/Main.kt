package main

import ch.vorburger.mariadb4j.DB
import ch.vorburger.mariadb4j.DBConfigurationBuilder
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
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
import resource.info
import resource.pokemon
import resource.store
import resource.user
import service.pokemon.PokemonService
import service.store.StoreService
import service.user.UserService
import utility.DatabaseFactory
import utility.ErrorLogger
import java.io.File

fun Application.module() {
    install(DefaultHeaders)

    install(CallLogging)

    install(WebSockets)

    install(CORS) {
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        header(HttpHeaders.AccessControlAllowOrigin)
        header(HttpHeaders.Authorization)
        anyHost()
    }

    install(ContentNegotiation) {
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
            registerModule(JodaModule())
        }
    }

    ErrorLogger.setupSentry()

    DatabaseFactory.init()

    install(Routing) {
        info()
        user(UserService())
        store(StoreService())
        pokemon(PokemonService())
    }
}

fun main() {
	val db = startDatabase();
	try{
	    embeddedServer(
	        factory = Netty,
	        port = System.getenv("backend_port").toInt(),
	        watchPaths = listOf("MainKt"),
	        module = Application::module
	    ).start()
	} catch (e: Exception) {
		db.stop()
	}
}

fun startDatabase() : DB {
	val configBuilder = DBConfigurationBuilder.newBuilder();
	configBuilder.setPort(3307); // PLEASE COMMENT THIS LINE IN PRODUCTION ENVIRONMENTS!!
	configBuilder.setDatabaseVersion("mariadb-10.2.11");
	val dbLoc = File("/home/simon/.pokeclicker/database");
	dbLoc.mkdirs();
	configBuilder.setDataDir(dbLoc.absolutePath); 
	var db = DB.newEmbeddedDB(configBuilder.build());
	db.start();
	if(!File("/home/simon/.pokeclicker/database/Selenium").exists())
		db.createDB("pokeclicker");
	System.out.println("Started database at port: "+db.getConfiguration().getPort());
	return db;
}