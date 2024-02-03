package pl.radoslav.bluetooth

data class BtDevice(
    val name: String,
    val address: String,
    val discoveryTimestamp: Long,
)
