package utility

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.Item
import model.Pokemon
import model.User
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    private val databaseHost = System.getenv("pokeclicker_db_host")
    private val databasePort = System.getenv("pokeclicker_db_port")
    private val databaseName = System.getenv("pokeclicker_db_name")
    private val databaseUser = System.getenv("pokeclicker_db_username")
    private val databasePassword = System.getenv("pokeclicker_db_password")

    fun init() {
        Database.connect(hikari())
        transaction {
            SchemaUtils.create(Item, Pokemon, User)
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

    suspend fun <T> dbQuery(block: () -> T) {
        withContext(Dispatchers.IO) {
            transaction { block() }
        }
    }
}