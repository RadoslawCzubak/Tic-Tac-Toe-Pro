package pl.radoslav.game.implementation.game.presentation

import pl.radoslav.game.implementation.game.domain.Board
import pl.radoslav.game.implementation.game.domain.Player

data class GameScreenState(
    val currentPlayer: Player = Player.O,
    val board: Board = Board(),
)
