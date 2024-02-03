package pl.radoslav.game.implementation.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.radoslav.game.implementation.data.HostGameRepositoryImpl
import pl.radoslav.game.implementation.domain.GameRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class GameModule {
    @Binds
    abstract fun bindGameRepository(implementation: HostGameRepositoryImpl): GameRepository
}
