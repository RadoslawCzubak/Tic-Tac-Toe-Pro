package pl.radoslav.tictactoe.feature.findgame.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

abstract class BluetoothConnection<T>(
    private val bluetoothAdapter: BluetoothAdapter,
    private val bluetoothScope: CoroutineScope
) {
    protected val inputStream: InputStream? = null
    protected var outputStream: OutputStream? = null
    protected val mmBuffer: ByteArray = ByteArray(1024)

    abstract val connectionState: MutableStateFlow<T>
    private var manageConnectionJob: Job? = null

    protected fun manageConnection(connectionSocket: BluetoothSocket): Flow<BluetoothMessage> {
        val inputStream = connectionSocket.inputStream
        outputStream = connectionSocket.outputStream
        val byteBuffer = ByteArray(1024)
        return callbackFlow {
            manageConnectionJob = bluetoothScope.launch {
                while (isActive) {
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

    fun disconnect() {
        manageConnectionJob?.cancel()
        outputStream?.apply {
            flush()
            close()
            outputStream = null
        }
    }
}

@SuppressLint("MissingPermission")
class ServerBluetoothConnection<T>(
    private val bluetoothAdapter: BluetoothAdapter,
    bluetoothScope: CoroutineScope
) : BluetoothConnection<ServerBluetoothEvent>(bluetoothAdapter, bluetoothScope) {

    override val connectionState: MutableStateFlow<ServerBluetoothEvent> =
        MutableStateFlow(ServerBluetoothEvent.WaitingForClient)

    private val serverSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
        bluetoothAdapter.listenUsingRfcommWithServiceRecord(
            "TicTacToe-Game",
            UUID.randomUUID()
        )
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
}

//@SuppressLint("MissingPermission")
//class ClientBluetoothConnection(
//    private val bluetoothAdapter: BluetoothAdapter,
//    private val bluetoothScope: CoroutineScope
//) : BluetoothConnection<ClientBluetoothEvent>(bluetoothAdapter, bluetoothScope) {
//
//    override val connectionState: MutableStateFlow<ClientBluetoothEvent> =
//        MutableStateFlow(ClientBluetoothEvent.Initialized)
//
//    fun connectToServer(btDevice: BluetoothDevice) {
//        bluetoothScope.launch {
//            bluetoothAdapter.cancelDiscovery()
//            val connectionSocket = btDevice.createRfcommSocketToServiceRecord(UUID.randomUUID())
//            connectionSocket?.connect()
//            connectionSocket?.let {
//                connectionState.tryEmit(ClientBluetoothEvent.ConnectedToServer)
//            }
//            manageConnection(connectionSocket)
//                .onEach { ClientBluetoothEvent.MessageReceived(it.message, it.bytesRead) }
//                .catch { error ->
//                    connectionState.tryEmit(ClientBluetoothEvent.Error(error))
//                }
//
//            awaitC {
//                connectionSocket?.close()
//                disconnect()
//            }
//        }
//    }
//}