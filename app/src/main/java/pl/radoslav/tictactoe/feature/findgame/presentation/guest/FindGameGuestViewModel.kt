package pl.radoslav.tictactoe.feature.findgame.presentation.guest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.radoslav.tictactoe.feature.findgame.data.BluetoothRepositoryImpl
import pl.radoslav.tictactoe.feature.findgame.data.GameServer
import pl.radoslav.tictactoe.feature.findgame.domain.model.BtDevice
import pl.radoslav.tictactoe.feature.findgame.domain.usecase.DiscoverBluetoothDevices
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FindGameGuestViewModel
    @Inject
    constructor(
        private val discoverBluetoothDevices: DiscoverBluetoothDevices,
        private val gameServer: GameServer,
        private val repositoryImpl: BluetoothRepositoryImpl,
    ) : ViewModel() {
        private val _state by lazy { MutableStateFlow(FindGameGuestState()) }
        val state = _state.asStateFlow()

        private var discoverJob: Job? = null

        init {
            viewModelScope.launch {
                gameServer.gameState.collect {
                    if (it == GameServer.Events.Connected) {
                        _state.update {
                            it.copy(
                                navigateToGame = true,
                            )
                        }
                    }
                }
            }
        }

        fun connectToDevice(btDevice: BtDevice) {
            Timber.d("Connect to $btDevice")
            viewModelScope.launch {
                repositoryImpl.connectToServer(btDevice)
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
