package pl.radoslav.game.implementation.findgame.presentation.guest

import pl.radoslav.bluetooth.BtDevice

data class FindGameGuestState(
    val devicesFound: List<BtDevice> = emptyList(),
    val isSearching: Boolean = false,
    val navigateToGame: Boolean = false,
)
