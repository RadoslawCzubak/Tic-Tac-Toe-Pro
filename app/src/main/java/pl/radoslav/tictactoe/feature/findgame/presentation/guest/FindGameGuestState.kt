package pl.radoslav.tictactoe.feature.findgame.presentation.guest

import pl.radoslav.tictactoe.feature.findgame.domain.model.BtDevice

data class FindGameGuestState(
    val devicesFound: List<BtDevice> = emptyList(),
    val isSearching: Boolean = false,
    val navigateToGame: Boolean = false,
)
