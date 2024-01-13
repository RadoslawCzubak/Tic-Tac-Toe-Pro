package pl.radoslav.tictactoe.feature.findgame.presentation.host

import pl.radoslav.tictactoe.feature.findgame.domain.model.BtDevice

data class FindGameHostState(
    val devicesFound: List<BtDevice> = emptyList(),
    val isSearching: Boolean = false,
)
