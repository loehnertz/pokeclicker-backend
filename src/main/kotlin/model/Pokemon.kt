package model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime
import service.store.data.ThinPokemon
import utility.PokeApi

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
    val owner: User? = null,
    val xp: Int,
    val aquisitionDateTime: DateTime,
    var fatApiInfo: me.sargunvohra.lib.pokekotlin.model.Pokemon? = null,
    var thinApiInfo: ThinPokemon? = null
)

fun Pokemons.toPokemon(row: ResultRow): Pokemon {
    return Pokemon(
        id = row[Pokemons.id],
        pokeNumber = row[Pokemons.pokeNumber],
        owner = Users.getUser(row[Pokemons.owner]),
        xp = row[Pokemons.xp],
        aquisitionDateTime = row[Pokemons.aquisitionDateTime],
        thinApiInfo = PokeApi.getPokemon(row[Pokemons.id])
    )
}
