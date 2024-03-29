package pl.radoslav.bluetooth.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class BluetoothModule {
    companion object {
        @Provides
        fun provideBluetoothManager(
            @ApplicationContext context: Context,
        ): BluetoothManager = context.getSystemService(BluetoothManager::class.java)

        @Provides
        fun provideBluetoothAdapter(bluetoothManager: BluetoothManager): BluetoothAdapter = bluetoothManager.adapter
    }
}
