@file:Suppress("unused")

package model

import service.store.data.ThinPokemon

data class Boosterpack(
    val locationId: Int,
    val name: String,
    val price: Long,
    val hexColor: String,
    val pokemons: List<ThinPokemon>
)
