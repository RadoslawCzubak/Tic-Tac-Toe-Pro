package pl.radoslav.tictactoe.feature.findgame.domain.usecase

import kotlinx.coroutines.flow.Flow
import pl.radoslav.tictactoe.feature.findgame.data.ClientBluetoothEvent
import pl.radoslav.tictactoe.feature.findgame.data.ServerBluetoothEvent
import pl.radoslav.tictactoe.feature.findgame.domain.model.BtDevice

interface BluetoothRepository {
    fun discoverDevices(): Flow<List<BtDevice>>
    suspend fun connectToServer(btDevice: BtDevice): Flow<ClientBluetoothEvent>
    suspend fun createServer(): Flow<ServerBluetoothEvent>
}
