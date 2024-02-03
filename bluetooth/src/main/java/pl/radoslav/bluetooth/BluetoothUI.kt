package pl.radoslav.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent

object BluetoothUI {
    fun startAdvertising(activity: Activity) {
        val requestCode = 1
        val discoverableIntent: Intent =
            Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120)
            }
        activity.startActivityForResult(discoverableIntent, requestCode)
    }
}
