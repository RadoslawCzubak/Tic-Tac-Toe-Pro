package pl.radoslav.game.implementation.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.radoslav.bluetooth.GameServer
import pl.radoslav.game.implementation.game.domain.Player
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GameViewModel
    @Inject
    constructor(
        private val gameServer: GameServer,
    ) : ViewModel() {
        private val _state by lazy { MutableStateFlow(GameScreenState()) }
        val state = _state.asStateFlow()

        init {
            viewModelScope.launch {
                gameServer.gameState.collect {
                    if (it is GameServer.Events.Message) {
                        updateCells(it.row, it.column)
                    }
                }
            }
        }

        fun onBoardCellClicked(
            rowId: Int,
            columnId: Int,
        ) {
            val message = "$rowId $columnId"
            Timber.d(message)
            gameServer.write(message.toByteArray())
        }

        private fun updateCells(
            rowId: Int,
            columnId: Int,
        ) {
            _state.update {
                val updatedBoard = it.board.cells.toMutableList().map { it.toMutableList() }
                if (updatedBoard[rowId][columnId] != null) return
                updatedBoard[rowId][columnId] = it.currentPlayer
                it.copy(
                    currentPlayer = if (it.currentPlayer == Player.O) Player.X else Player.O,
                    board = it.board.copy(cells = updatedBoard),
                )
            }
        }
    }
