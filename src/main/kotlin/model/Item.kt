package model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import java.time.LocalDateTime

object Items : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val itemNumber = integer("itemNumber")
    val owner = reference("owner", refColumn = Users.id, onDelete = ReferenceOption.CASCADE)
    val aquisitionDateTime = datetime("aquisitionDateTime")
}

data class Item(
    val id: Int,
    val itemNumber: Int,
    val owner: Users,
    val aquisitionDateTime: LocalDateTime
)
