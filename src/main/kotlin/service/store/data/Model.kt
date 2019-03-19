package service.store.data

import java.math.BigDecimal

data class ThinPokemon(
    val id: Int,
    val name: String,
    var xp: BigDecimal,
    val sprite: String?
)
