package pl.radoslav.bluetooth

import kotlinx.coroutines.flow.Flow

interface BluetoothRepository {
    fun discoverDevices(): Flow<List<BtDevice>>

    suspend fun connectToServer(btDevice: BtDevice)
}
