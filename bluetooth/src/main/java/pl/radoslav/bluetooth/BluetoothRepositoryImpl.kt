package pl.radoslav.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothRepositoryImpl
    @Inject
    constructor(
        private val bluetoothService: BluetoothService,
        private val gameServer: GameServer,
    ) : BluetoothRepository {
        private val bluetoothDevicesCache = mutableListOf<BluetoothDevice>()

        @SuppressLint("MissingPermission")
        override fun discoverDevices(): Flow<List<BtDevice>> {
            bluetoothDevicesCache.clear()
            return bluetoothService.discoverDevices()
                .onEach {
                    bluetoothDevicesCache.add(it)
                }
                .map { BtDevice(it.name ?: "", it.address ?: "", System.currentTimeMillis()) }
                .scan(initial = emptyList()) { accumulatedDevices, newDevice ->
                    var updatedAccumulatedDevices: List<BtDevice> =
                        accumulatedDevices.toMutableList().apply {
                            removeIf {
                                it.address == newDevice.address
                            }
                        }
                    updatedAccumulatedDevices = updatedAccumulatedDevices + newDevice
                    updatedAccumulatedDevices
                }
        }

        override suspend fun connectToServer(btDevice: BtDevice) {
            val bluetoothDevice =
                bluetoothDevicesCache.find {
                    it.address == btDevice.address
                } ?: throw IllegalStateException("Device not found")
            gameServer.connectToGameServer(
                bluetoothDevice,
            )
        }
    }
