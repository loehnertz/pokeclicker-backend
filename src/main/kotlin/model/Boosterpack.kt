@file:Suppress("unused")

package model

import service.store.data.ThinPokemon
import java.math.BigDecimal

data class Boosterpack(
    val locationId: Int,
    val name: String,
    val price: BigDecimal,
    val hexColor: String,
    val pokemons: List<ThinPokemon>
)
