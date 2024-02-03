package pl.radoslav.game.implementation.findgame.presentation.guest

import pl.radoslav.game.implementation.domain.GameServer

data class FindGameClientState(
    val devicesFound: List<GameServer> = emptyList(),
    val isSearching: Boolean = false,
    val navigateToGame: Boolean = false,
)
