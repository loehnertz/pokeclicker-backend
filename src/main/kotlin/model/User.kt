@file:Suppress("unused")

package model

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object Users : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", 255)
    val avatarUri = varchar("avatarUri", 511).nullable()
    val pokeDollars = long("pokeDollars")
}

data class User(
    val id: Int,
    val name: String,
    val avatarUri: String?,
    val pokeDollars: Long
)

fun Users.toUser(row: ResultRow): User {
    return User(
        id = row[Users.id],
        name = row[Users.name],
        avatarUri = row[Users.avatarUri],
        pokeDollars = row[Users.pokeDollars]
    )
}

fun Users.getUser(userId: Int): User {
    return Users.toUser(
        transaction {
            Users.select { Users.id eq userId }.firstOrNull()
        } ?: throw Exception("No user with ID '$userId' exists")
    )
}
