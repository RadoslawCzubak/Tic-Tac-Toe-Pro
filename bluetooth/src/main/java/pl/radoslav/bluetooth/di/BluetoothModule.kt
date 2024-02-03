package pl.radoslav.bluetooth.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.radoslav.bluetooth.BluetoothRepository
import pl.radoslav.bluetooth.BluetoothRepositoryImpl

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

    @Binds
    abstract fun bindBluetoothRepository(bluetoothRepositoryImpl: BluetoothRepositoryImpl): BluetoothRepository
}
