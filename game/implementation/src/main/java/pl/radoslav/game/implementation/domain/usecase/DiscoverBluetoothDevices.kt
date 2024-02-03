package pl.radoslav.game.implementation.domain.usecase

import android.util.Log
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import pl.radoslav.game.implementation.domain.GameRepository
import pl.radoslav.game.implementation.domain.GameServer
import javax.inject.Inject

class DiscoverBluetoothDevices
    @Inject
    constructor(
        private val repository: GameRepository,
    ) {
        suspend operator fun invoke(): Flow<Resource> =
            repository.findServers()
                .map { Resource.Success(it) as Resource }
                .catch {
                    if (it is TimeoutCancellationException) {
                        emit(Resource.Closed)
                    }
                    emit(Resource.Failure("Error while searching for devices: ${it.message}"))
                    Log.d("DiscoverBluetoothDevices", "Error while searching for devices: ${it.message}")
                }

        sealed interface Resource {
            data class Success(val devices: List<GameServer>) : Resource

            data class Failure(val message: String) : Resource

            data object Closed : Resource
        }
    }
