@file:Suppress("unused")

package model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime

object Items : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val itemNumber = integer("itemNumber")
    val owner = reference("owner", refColumn = Users.id, onDelete = ReferenceOption.CASCADE)
    val aquisitionDateTime = datetime("aquisitionDateTime")
}

data class Item(
    val id: Int,
    val itemNumber: Int,
    val owner: User?,
    val aquisitionDateTime: DateTime
)

fun Items.toItem(row: ResultRow): Item {
    return Item(
        id = row[Items.id],
        itemNumber = row[Items.itemNumber],
        owner = null,
        aquisitionDateTime = row[Items.aquisitionDateTime]
    )
}
