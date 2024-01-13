package pl.radoslav.tictactoe.feature.findgame.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.timeout
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class BluetoothService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val bluetoothAdapter: BluetoothAdapter,
) {
    @OptIn(FlowPreview::class)
    @SuppressLint("MissingPermission")
    fun discoverDevices(): Flow<BluetoothDevice> {
        return callbackFlow {
            val receiver = object : BroadcastReceiver() {
                @SuppressLint("MissingPermission")
                override fun onReceive(context: Context, intent: Intent) {
                    val action: String = intent.action ?: ""
                    when (action) {
                        BluetoothDevice.ACTION_FOUND -> {
                            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                                ?.let {
                                    trySend(it)
                                }
                        }
                    }
                }
            }
            context.registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
            bluetoothAdapter.startDiscovery()
            awaitClose {
                context.unregisterReceiver(receiver)
                bluetoothAdapter.cancelDiscovery()
            }
        }.timeout(timeout = 15.seconds)
            .cancellable()
    }
}