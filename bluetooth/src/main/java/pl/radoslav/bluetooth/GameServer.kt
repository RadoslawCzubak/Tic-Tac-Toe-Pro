package pl.radoslav.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("MissingPermission")
@Singleton
class GameServer
    @Inject
    constructor(
        private val bluetoothAdapter: BluetoothAdapter,
    ) {
        sealed interface Events {
            data object Empty : Events

            data object Connected : Events

            data class Message(val row: Int, val column: Int) : Events
        }

        private val _gameState = MutableStateFlow<Events>(Events.Empty)
        val gameState = _gameState.asStateFlow()

        private var serverThread: ServerThread? = null
        private var clientThread: ConnectThread? = null
        private var connectionThread: ConnectedThread? = null

        @SuppressLint("HandlerLeak")
        private val handler =
            object : Handler() {
                override fun handleMessage(msg: Message) {
                    when (msg.what) {
                        MESSAGE_READ -> {
                            val readBuf = msg.obj as ByteArray
                            val readMessage = String(readBuf, 0, msg.arg1)
                            val (row, column) = readMessage.split(" ")
                            _gameState.tryEmit(Events.Message(row.toInt(), column.toInt()))
                        }

                        MESSAGE_WRITE -> {
                        }

                        else -> Unit
                    }
                }
            }

        fun createGameServer() {
            serverThread?.cancel()
            serverThread = null
            clientThread?.cancel()
            clientThread = null

            serverThread = ServerThread()
            serverThread?.start()
        }

        fun connectToGameServer(bluetoothDevice: BluetoothDevice) {
            serverThread?.cancel()
            serverThread = null
            clientThread?.cancel()
            clientThread = null

            clientThread = ConnectThread(bluetoothDevice)
            clientThread?.start()
        }

        private inner class ServerThread : Thread() {
            private val serverSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
                bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                    "TicTacToeServer",
                    TIC_TAC_TOE_SERVER_UUID,
                )
            }

            override fun run() {

            }

            fun cancel() {
                try {
                    serverSocket?.close()
                } catch (exception: IOException) {
                    Timber.e("Could not close the connect socket", exception)
                }
            }
        }

        private inner class ConnectThread(device: BluetoothDevice) : Thread() {
            private val socket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
                device.createRfcommSocketToServiceRecord(TIC_TAC_TOE_SERVER_UUID)
            }

            override fun run() {
                bluetoothAdapter.cancelDiscovery()

                socket?.let { socket ->
                    socket.connect()
                    manageMyConnectedSocket(socket)
                }
            }

            fun cancel() {
                try {
                    socket?.close()
                } catch (exception: IOException) {
                    Timber.e("Could not close the client socket", exception)
                }
            }
        }

        private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
            private val mmInStream: InputStream = mmSocket.inputStream
            private val mmOutStream: OutputStream = mmSocket.outputStream
            private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

            override fun run() {
                var numBytes: Int
                while (true) {
                    numBytes =
                        try {
                            Timber.d("Reading...")
                            mmInStream.read(mmBuffer)
                        } catch (e: IOException) {
                            Timber.d("Input stream was disconnected", e)
                            break
                        }
                    handler.obtainMessage(
                        MESSAGE_READ,
                        numBytes,
                        -1,
                        mmBuffer,
                    ).sendToTarget()
                }
            }

            fun write(bytes: ByteArray) {
                try {
                    mmOutStream.write(bytes)
                    Timber.d("Writing...")
                } catch (e: IOException) {
                    Timber.e("Error occurred when sending data", e)
                }
            }

            fun cancel() {
                try {
                    mmSocket.close()
                } catch (e: IOException) {
                    Timber.e("Could not close the connect socket", e)
                }
            }
        }

        fun write(out: ByteArray?) {
            var r: ConnectedThread?
            Timber.d("Try to write")
            synchronized(this) {
                if (connectionThread != null) {
                    r = connectionThread!!
                } else {
                    return
                }
            }
            r?.write(out!!)
        }

        fun manageMyConnectedSocket(bluetoothSocket: BluetoothSocket) {
            _gameState.tryEmit(Events.Connected)
            connectionThread = ConnectedThread(bluetoothSocket)
            connectionThread?.start()
        }

        companion object {
            val TIC_TAC_TOE_SERVER_UUID: UUID =
                UUID.fromString("483f9bd2-57f5-48f6-ab45-1748edfa2a77")

            const val MESSAGE_READ = 1
            const val MESSAGE_WRITE = 2
        }
    }
