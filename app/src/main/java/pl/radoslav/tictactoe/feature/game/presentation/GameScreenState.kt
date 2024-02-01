package pl.radoslav.tictactoe.feature.game.presentation

import pl.radoslav.tictactoe.feature.game.domain.Board
import pl.radoslav.tictactoe.feature.game.domain.Player

data class GameScreenState(
    val currentPlayer: Player = Player.O,
    val board: Board = Board()
)