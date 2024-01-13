package pl.radoslav.tictactoe.feature.findgame.data

import android.annotation.SuppressLint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import pl.radoslav.tictactoe.feature.findgame.domain.model.BtDevice
import pl.radoslav.tictactoe.feature.findgame.domain.usecase.BluetoothRepository
import javax.inject.Inject

class BluetoothRepositoryImpl @Inject constructor(
    private val bluetoothService: BluetoothService
) : BluetoothRepository {
    private val threeSecondsInMs = 3000L

    @SuppressLint("MissingPermission")
    override fun discoverDevices(): Flow<List<BtDevice>> =
        bluetoothService.discoverDevices()
            .map { BtDevice(it.name ?: "", it.address ?: "", System.currentTimeMillis()) }
            .scan(initial = emptyList()) { accumulatedDevices, newDevice ->
                var updatedAccumulatedDevices: List<BtDevice> =
                    accumulatedDevices.toMutableList().apply {
                        removeIf {
                            it.address == newDevice.address
                        }
                    }
                updatedAccumulatedDevices = updatedAccumulatedDevices + newDevice
//                updatedAccumulatedDevices.filter {
//                    it.discoveryTimestamp > (System.currentTimeMillis() - threeSecondsInMs)
//                }
                updatedAccumulatedDevices
            }
}