package pl.radoslav.tictactoe.feature.findgame.presentation.host

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.radoslav.tictactoe.feature.findgame.data.GameServer
import javax.inject.Inject

@HiltViewModel
class FindGameHostViewModel @Inject constructor(
    private val gameServer: GameServer
) : ViewModel() {

    private val _state by lazy { MutableStateFlow(FindGameHostState()) }
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            gameServer.createGameServer()
        }
        viewModelScope.launch {
            gameServer.gameState.collect {
                if (it == GameServer.Events.Connected)
                    _state.update {
                        it.copy(navigateToGame = true)
                    }
            }
        }
    }
}