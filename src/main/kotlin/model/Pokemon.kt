package model

import org.jetbrains.exposed.sql.CurrentDateTime
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Pokemon : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val pokeNumber = integer("pokeNumber")
    val owner = reference("owner", refColumn = User.id, onDelete = ReferenceOption.CASCADE)
    val xp = integer("xp")
    val aquisitionDateTime = datetime("aquisitionDateTime").defaultExpression(CurrentDateTime())
}
