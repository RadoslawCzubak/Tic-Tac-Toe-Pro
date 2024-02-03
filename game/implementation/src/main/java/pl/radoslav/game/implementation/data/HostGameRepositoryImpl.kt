package pl.radoslav.game.implementation.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import pl.radoslav.bluetooth.BluetoothService
import pl.radoslav.game.implementation.domain.GameRepository
import pl.radoslav.game.implementation.domain.GameServer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@SuppressLint("MissingPermission")
class HostGameRepositoryImpl
    @Inject
    constructor(
        private val bluetoothService: BluetoothService,
    ) : GameRepository {
        private val cachedDevices = mutableListOf<BluetoothDevice>()

        override fun findServers(): Flow<List<GameServer>> {
            cachedDevices.clear()
            return bluetoothService.discoverDevices()
                .onEach { cachedDevices.add(it) }
                .map { bluetoothDevice ->
                    GameServer(bluetoothDevice.name, bluetoothDevice.address)
                }
                .scan(emptyList()) { gameServerList, newGameServer ->
                    val list = gameServerList.toMutableList()
                    list.removeIf {
                        it.address == newGameServer.address
                    }
                    list.add(newGameServer)
                    list
                }
        }
    }
