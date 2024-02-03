package pl.radoslav.game.implementation.findgame.presentation.guest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.radoslav.game.implementation.domain.GameRepository
import pl.radoslav.game.implementation.domain.GameServer
import pl.radoslav.game.implementation.domain.usecase.DiscoverBluetoothDevices
import javax.inject.Inject

@HiltViewModel
class FindGameClientViewModel
    @Inject
    constructor(
        private val discoverBluetoothDevices: DiscoverBluetoothDevices,
        private val gameRepository: GameRepository,
    ) : ViewModel() {
        private val _state by lazy { MutableStateFlow(FindGameClientState()) }
        val state = _state.asStateFlow()

        private var discoverJob: Job? = null

        fun connectToDevice(server: GameServer) {
            viewModelScope.launch {
            }
        }

        fun discoverDevices() {
            if (discoverJob != null) return
            discoverJob =
                viewModelScope.launch {
                    _state.update { it.copy(isSearching = true) }
                    discoverBluetoothDevices()
                        .collect { resource ->
                            when (resource) {
                                is DiscoverBluetoothDevices.Resource.Success -> {
                                    _state.update { it.copy(devicesFound = resource.devices) }
                                }

                                is DiscoverBluetoothDevices.Resource.Failure -> {
                                    stopDiscoveringDevices()
                                }

                                is DiscoverBluetoothDevices.Resource.Closed -> {
                                    stopDiscoveringDevices()
                                }
                            }
                        }
                }
        }

        fun stopDiscoveringDevices() {
            discoverJob?.cancel()
            discoverJob = null
            _state.update { it.copy(isSearching = false) }
        }
    }
