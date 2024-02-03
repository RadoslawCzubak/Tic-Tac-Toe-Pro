package pl.radoslav.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.RequiresPermission
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

class BluetoothService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
) {
    private var bluetoothConnection: BluetoothConnection? = null

    @SuppressLint("InlinedApi")
    @OptIn(FlowPreview::class)
    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_SCAN])
    fun discoverDevices(): Flow<BluetoothDevice> {
        return callbackFlow {
            val receiver =
                object : BroadcastReceiver() {
                    @SuppressLint("MissingPermission")
                    override fun onReceive(
                        context: Context,
                        intent: Intent,
                    ) {
                        when (intent.action ?: "") {
                            BluetoothDevice.ACTION_FOUND -> {
                                intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                                    ?.let {
                                        Timber.d(it.toString())
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

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    suspend fun createBluetoothServer() {
        val serverSocket: BluetoothServerSocket? = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
            "TicTacToeServer",
            TIC_TAC_TOE_SERVER_UUID,
        )
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
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    suspend fun connectToDevice(bluetoothDevice: BluetoothDevice) {
        val socket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothDevice.createRfcommSocketToServiceRecord(TIC_TAC_TOE_SERVER_UUID)
        }
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
    }

    private fun createConnection(bluetoothSocket: BluetoothSocket) {
        bluetoothConnection = BluetoothConnection(bluetoothSocket)
    }

    companion object {
        val TIC_TAC_TOE_SERVER_UUID: UUID =
            UUID.fromString("483f9bd2-57f5-48f6-ab45-1748edfa2a77")
    }
}
