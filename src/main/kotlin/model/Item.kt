package model

import org.jetbrains.exposed.sql.CurrentDateTime
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object Item : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val itemNumber = integer("itemNumber")
    val owner = reference("owner", refColumn = User.id, onDelete = ReferenceOption.CASCADE)
    val aquisitionDateTime = datetime("aquisitionDateTime").defaultExpression(CurrentDateTime())
}
