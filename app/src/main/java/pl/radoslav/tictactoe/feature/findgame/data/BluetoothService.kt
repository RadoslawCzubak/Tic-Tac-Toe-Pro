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
    private val serverSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
        bluetoothAdapter.listenUsingRfcommWithServiceRecord(
            "TicTacToe-Game",
            UUID.randomUUID()
        )
    }

    //    private var connectionSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private val mmBuffer: ByteArray = ByteArray(1024)

    private val bluetoothScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var manageConnectionJob: Job? = null

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

    fun createNewServer(): Flow<ServerBluetoothEvent> {
        return callbackFlow {
            val connectionSocket = try {
                setupServer()
            } catch (e: Exception) {
                Log.d("BluetoothService", "Error while setting up server: ${e.message}")
                trySend(ServerBluetoothEvent.Error(e))
                null
            }
            trySend(ServerBluetoothEvent.ClientConnected)
            try {
                connectionSocket?.let {
                    manageConnection(it)
                        .onEach { bluetoothMessage ->
                            trySend(
                                ServerBluetoothEvent.MessageReceived(
                                    bluetoothMessage.message,
                                    bluetoothMessage.bytesRead
                                )
                            )
                        }.catch { error ->
                            trySend(ServerBluetoothEvent.Error(error))
                        }
                } ?: throw IllegalStateException("Connection socket is null")
            } catch (e: Exception) {
                Log.d("BluetoothService", "Error while managing connection: ${e.message}")
                trySend(ServerBluetoothEvent.Error(e))
            }
            awaitClose {
                connectionSocket?.close()
                disconnect()
            }
        }
    }

    private suspend fun setupServer(): BluetoothSocket? =
        withContext(Dispatchers.IO) {
            var isWaiting = true
            while (isWaiting) {
                val connectionSocket = try {
                    serverSocket?.accept()
                } catch (e: Exception) {
                    Log.d("BluetoothService", "Error while accepting connection: ${e.message}")
                    isWaiting = false
                    return@withContext null
                }
                connectionSocket?.also {
                    serverSocket?.close()
                    isWaiting = false
                    return@withContext it
                }
            }
            null
        }


    fun connectToServer(btDevice: BluetoothDevice): Flow<ClientBluetoothEvent> {
        return callbackFlow {
            bluetoothAdapter.cancelDiscovery()
            val connectionSocket = btDevice.createRfcommSocketToServiceRecord(UUID.randomUUID())
            connectionSocket?.connect()
            connectionSocket?.let {
                trySend(ClientBluetoothEvent.ConnectedToServer)
            }
            manageConnection(connectionSocket)
                .onEach { ClientBluetoothEvent.MessageReceived(it.message, it.bytesRead) }
                .catch { error ->
                    trySend(ClientBluetoothEvent.Error(error))
                }

            awaitClose {
                connectionSocket?.close()
                disconnect()
            }
        }
    }


    private fun manageConnection(connectionSocket: BluetoothSocket): Flow<BluetoothMessage> {
        val inputStream = connectionSocket.inputStream
        outputStream = connectionSocket.outputStream
        val byteBuffer = ByteArray(1024)
        return callbackFlow {
            manageConnectionJob = bluetoothScope.launch {
                while (true) {
                    val numberOfBytesRead = try {
                        inputStream?.read(mmBuffer)
                    } catch (e: Exception) {
                        Log.d(
                            "BluetoothService",
                            "Error while reading from input stream: ${e.message}"
                        )
                    }
                    trySend(BluetoothMessage(byteBuffer, numberOfBytesRead ?: 0))
                }
            }
        }
    }

    private fun sendMessage(bytes: ByteArray) {
        try {
            outputStream?.write(bytes)
        } catch (e: Exception) {
            Log.d("BluetoothService", "Error while writing to output stream: ${e.message}")
        }
    }

    private fun disconnect() {
        manageConnectionJob?.cancel()
        outputStream?.apply {
            flush()
            close()
            outputStream = null
        }
    }
}

data class BluetoothMessage(
    val message: ByteArray,
    val bytesRead: Int,
)

sealed interface ServerBluetoothEvent {
    data object ClientConnected : ServerBluetoothEvent
    data object ClientDisconnected : ServerBluetoothEvent
    data class MessageReceived(val bytes: ByteArray, val numberOfBytesRead: Int) :
        ServerBluetoothEvent

    data class Error(val throwable: Throwable) : ServerBluetoothEvent
}

sealed interface ClientBluetoothEvent {
    data object ConnectedToServer : ClientBluetoothEvent
    data object DisconnectedFromServer : ClientBluetoothEvent
    data class MessageReceived(val bytes: ByteArray, val numberOfBytesRead: Int) :
        ClientBluetoothEvent

    data class Error(val throwable: Throwable) : ClientBluetoothEvent
}