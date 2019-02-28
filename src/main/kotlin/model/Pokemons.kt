package model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import java.time.LocalDateTime

object Pokemons : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val pokeNumber = integer("pokeNumber")
    val owner = reference("owner", refColumn = Users.id, onDelete = ReferenceOption.CASCADE)
    val xp = integer("xp")
    val aquisitionDateTime = datetime("aquisitionDateTime")
}

data class Pokemon(
    val id: Int,
    val pokeNumber: Int,
    val owner: User,
    val xp: Int,
    val aquisitionDateTime: LocalDateTime
)
