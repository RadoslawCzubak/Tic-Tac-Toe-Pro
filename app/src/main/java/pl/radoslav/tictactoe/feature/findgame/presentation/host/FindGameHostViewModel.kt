package pl.radoslav.tictactoe.feature.findgame.presentation.host

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.radoslav.tictactoe.feature.findgame.domain.usecase.DiscoverBluetoothDevices
import javax.inject.Inject

@HiltViewModel
class FindGameHostViewModel @Inject constructor(
    private val discoverBluetoothDevices: DiscoverBluetoothDevices
) : ViewModel() {
    private val _state by lazy { MutableStateFlow(FindGameHostState()) }
    val state = _state.asStateFlow()

    private var discoverJob: Job? = null

    fun discoverDevices() {
        if (discoverJob != null) return
        discoverJob = viewModelScope.launch {
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
