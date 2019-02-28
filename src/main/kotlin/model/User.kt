package model

import org.jetbrains.exposed.sql.Table

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
