package pl.radoslav.game.implementation.game.domain

data class Board(
    val cells: List<List<Player?>> = List(3) { List(3) { null } },
)
