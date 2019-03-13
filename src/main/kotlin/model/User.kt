@file:Suppress("unused")

package model

import io.ktor.features.NotFoundException
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import service.user.balance.BalanceManager

object Users : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", 255).uniqueIndex()
    val email = varchar("email", 511).uniqueIndex()
    val password = varchar("password", 255)
    val avatarUri = varchar("avatarUri", 511).nullable()
    val pokeDollars = long("pokeDollars").default(0)
}

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val avatarUri: String? = null,
    val pokeDollars: Long
)

fun Users.toUser(row: ResultRow): User {
    return User(
        id = row[Users.id],
        name = row[Users.name],
        email = row[Users.email],
        password = row[Users.password],
        avatarUri = row[Users.avatarUri],
        pokeDollars = row[Users.pokeDollars]
    )
}

fun Users.getUser(userId: Int): User {
    return Users.toUser(
        transaction {
            Users.select { Users.id eq userId }.firstOrNull()
        } ?: throw NotFoundException("No user with ID '$userId' exists")
    )
}

fun Users.getUser(username: String): User {
    return Users.toUser(
        transaction {
            Users.select { Users.name eq username }.firstOrNull()
        } ?: throw NotFoundException("No user with the name '$username' exists")
    )
}

fun Users.getPokemons(userId: Int): List<Pokemon> {
    return transaction { Pokemons.select { Pokemons.owner eq userId }.map { Pokemons.toPokemon(it) } }
}

fun Users.subtractPokeDollarsFromBalance(userId: Int, amountToSubtract: Long): Long {
    val user = Users.getUser(userId)

    transaction {
        Users.update({ Users.id eq user.id }) { it[pokeDollars] = (user.pokeDollars - amountToSubtract) }
    }

    val newBalance = (user.pokeDollars - amountToSubtract)

    BalanceManager(user).setCurrentBalance(newBalance)

    return newBalance
}
