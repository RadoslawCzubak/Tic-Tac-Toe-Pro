package pl.radoslav.game.implementation.domain

import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun findServers(): Flow<List<GameServer>>
}
