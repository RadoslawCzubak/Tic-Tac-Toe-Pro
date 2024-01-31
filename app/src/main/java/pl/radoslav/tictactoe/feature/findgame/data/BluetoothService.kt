package pl.radoslav.tictactoe.feature.findgame.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.seconds

@SuppressLint("MissingPermission")
class BluetoothService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val bluetoothAdapter: BluetoothAdapter,
) {
    private val bluetoothScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    @OptIn(FlowPreview::class)
    @SuppressLint("MissingPermission")
    fun discoverDevices(): Flow<BluetoothDevice> {
        return callbackFlow {
            val receiver = object : BroadcastReceiver() {
                @SuppressLint("MissingPermission")
                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action ?: "") {
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

data class BluetoothMessage(
    val message: ByteArray,
    val bytesRead: Int,
)

sealed interface ServerBluetoothEvent {
    data object WaitingForClient : ServerBluetoothEvent
    data object ClientConnected : ServerBluetoothEvent
    data object ClientDisconnected : ServerBluetoothEvent
    data class MessageReceived(val bytes: ByteArray, val numberOfBytesRead: Int) :
        ServerBluetoothEvent

    data class Error(val throwable: Throwable) : ServerBluetoothEvent
}

sealed interface ClientBluetoothEvent {
    data object Initialized: ClientBluetoothEvent
    data object ConnectedToServer : ClientBluetoothEvent
    data object DisconnectedFromServer : ClientBluetoothEvent
    data class MessageReceived(val bytes: ByteArray, val numberOfBytesRead: Int) :
        ClientBluetoothEvent

    data class Error(val throwable: Throwable) : ClientBluetoothEvent
}