package model

import org.jetbrains.exposed.sql.Table

object User : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", 255)
    val avatarUri = varchar("avatarUri", 511).nullable()
    val pokeDollars = long("pokeDollars")
}
