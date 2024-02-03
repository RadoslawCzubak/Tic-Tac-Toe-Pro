package pl.radoslav.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
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
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

@SuppressLint("MissingPermission")
class BluetoothService
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val bluetoothAdapter: BluetoothAdapter,
    ) {
        var bluetoothConnection: BluetoothConnection? = null
            private set

        @SuppressLint("InlinedApi")
        @OptIn(FlowPreview::class)
        fun discoverDevices(): Flow<BluetoothDevice> {
            return callbackFlow {
                val receiver =
                    object : BroadcastReceiver() {
                        override fun onReceive(
                            context: Context,
                            intent: Intent,
                        ) {
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

        suspend fun createBluetoothServer() {
            val serverSocket: BluetoothServerSocket? =
                bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                    "TicTacToeServer",
                    TIC_TAC_TOE_SERVER_UUID,
                )
            val btSocket =
                suspendCancellableCoroutine { cont ->
                    var shouldLoop = true
                    while (shouldLoop) {
                        val socket: BluetoothSocket? =
                            try {
                                serverSocket?.accept()
                            } catch (exception: IOException) {
                                shouldLoop = false

                                null
                            }
                        socket?.also {
                            cont.resume(it)
                            serverSocket?.close()
                            shouldLoop = false
                        }
                        if (!cont.isActive) break
                    }
                    cont.invokeOnCancellation {
                        try {
                            serverSocket?.close()
                        } catch (exception: IOException) {
                            Timber.e("Could not close the connect socket", exception)
                        }
                    }
                }
            createConnection(btSocket)
        }

        suspend fun connectToDevice(bluetoothDevice: BluetoothDevice) {
            val socket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
                bluetoothDevice.createRfcommSocketToServiceRecord(TIC_TAC_TOE_SERVER_UUID)
            }
            val btSocket =
                suspendCancellableCoroutine { cont ->
                    bluetoothAdapter.cancelDiscovery()

                    socket?.let { socket ->
                        socket.connect()
                        cont.resume(socket)
                    }

                    cont.invokeOnCancellation {
                        try {
                            socket?.close()
                        } catch (exception: IOException) {
                            Timber.e("Could not close the client socket", exception)
                        }
                    }
                }
            createConnection(btSocket)
        }

        private fun createConnection(bluetoothSocket: BluetoothSocket) {
            bluetoothConnection = BluetoothConnection(bluetoothSocket)
        }

        fun observeBluetoothState(): Flow<BluetoothState> =
            callbackFlow {
                val receiver =
                    object : BroadcastReceiver() {
                        override fun onReceive(
                            context: Context,
                            intent: Intent,
                        ) {
                            when (intent.action ?: "") {
                                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                                    intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                                        .let {
                                            when (it) {
                                                BluetoothAdapter.STATE_ON -> BluetoothState.On
                                                BluetoothAdapter.STATE_TURNING_ON -> BluetoothState.TurningOn
                                                BluetoothAdapter.STATE_TURNING_OFF -> BluetoothState.TurningOff
                                                BluetoothAdapter.STATE_OFF -> BluetoothState.Off
                                                else -> Unit
                                            }
                                        }
                                }
                            }
                        }
                    }
                context.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
                awaitClose {
                    context.unregisterReceiver(receiver)
                }
            }

        companion object {
            val TIC_TAC_TOE_SERVER_UUID: UUID =
                UUID.fromString("483f9bd2-57f5-48f6-ab45-1748edfa2a77")
        }
    }

sealed interface BluetoothState {
    data object On : BluetoothState

    data object TurningOn : BluetoothState

    data object TurningOff : BluetoothState

    data object Off : BluetoothState
}
