package pl.radoslav.tictactoe.feature.game.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pl.radoslav.tictactoe.feature.game.domain.Player
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor() : ViewModel() {

    private val _state by lazy { MutableStateFlow(GameScreenState()) }
    val state = _state.asStateFlow()

    fun onBoardCellClicked(rowId: Int, columnId: Int) {
        _state.update {
            val updatedBoard = it.board.cells.toMutableList().map { it.toMutableList() }
            if(updatedBoard[rowId][columnId] != null) return
            updatedBoard[rowId][columnId] = it.currentPlayer
            it.copy(
                currentPlayer = if (it.currentPlayer == Player.O) Player.X else Player.O,
                board = it.board.copy(cells = updatedBoard)
            )
        }
    }
}