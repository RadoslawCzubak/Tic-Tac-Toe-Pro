package pl.radoslav.bluetooth

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BluetoothConnection(
    private val bluetoothSocket: BluetoothSocket,
) {
    private val incomingMessagesStream: InputStream = bluetoothSocket.inputStream
    private val sentMessagesStream: OutputStream = bluetoothSocket.outputStream
    private val messageBuffer: ByteArray = ByteArray(1024)

    fun listenToMessages(): Flow<BTEvent> {
        return callbackFlow {
            var numBytes: Int
            while (true) {
                numBytes =
                    try {
                        Timber.d("Reading...")
                        incomingMessagesStream.read(messageBuffer)
                    } catch (e: IOException) {
                        Timber.d("Input stream was disconnected", e)
                        break
                    }
                trySend(
                    BTEvent.Message(
                        numBytes,
                        messageBuffer,
                    ),
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    fun write(bytes: ByteArray) {
        try {
            sentMessagesStream.write(bytes)
        } catch (e: IOException) {
            Timber.e("Error occurred when sending data", e)
        }
    }

    fun close() {
        try {
            bluetoothSocket.close()
        } catch (e: IOException) {
            Timber.e("Could not close the connect socket", e)
        }
    }

    companion object {
        sealed interface BTEvent {
            data class Message(
                val bytesRead: Int,
                val byteArray: ByteArray,
            ) : BTEvent
        }
    }
}
