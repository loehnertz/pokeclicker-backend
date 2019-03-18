package service.store.data

data class ThinPokemon(
    val id: Int,
    val name: String,
    var xp: Long,
    val sprite: String?
)
