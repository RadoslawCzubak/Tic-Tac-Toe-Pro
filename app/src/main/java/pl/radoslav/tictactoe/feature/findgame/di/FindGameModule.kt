package pl.radoslav.tictactoe.feature.findgame.di

import android.bluetooth.BluetoothManager
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.radoslav.tictactoe.feature.findgame.data.BluetoothRepositoryImpl
import pl.radoslav.tictactoe.feature.findgame.domain.usecase.BluetoothRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class FindGameModule {
    companion object {
        @Provides
        fun provideBluetoothManager(@ApplicationContext context: Context): BluetoothManager =
            context.getSystemService(BluetoothManager::class.java)

        @Provides
        fun provideBluetoothAdapter(bluetoothManager: BluetoothManager) =
            bluetoothManager.adapter
    }

    @Binds
    abstract fun bindBluetoothRepository(
        bluetoothRepositoryImpl: BluetoothRepositoryImpl
    ): BluetoothRepository
}