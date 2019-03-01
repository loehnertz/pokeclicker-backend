package utility

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import model.Items
import model.Pokemons
import model.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    private val databaseHost = System.getenv("db_host")
    private val databasePort = System.getenv("db_port")
    private val databaseName = System.getenv("db_name")
    private val databaseUser = System.getenv("db_username")
    private val databasePassword = System.getenv("db_password")

    fun init() {
        Database.connect(hikari())
        transaction {
            SchemaUtils.create(Items, Pokemons, Users)
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "com.mysql.cj.jdbc.Driver"
        config.jdbcUrl = "jdbc:mysql://$databaseHost:$databasePort/$databaseName"
        config.username = databaseUser
        config.password = databasePassword
        config.validate()
        return HikariDataSource(config)
    }
}
