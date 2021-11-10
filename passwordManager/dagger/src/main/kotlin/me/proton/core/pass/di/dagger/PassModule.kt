package me.proton.core.pass.di.dagger

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.pass.data.db.PassDatabase
import me.proton.core.pass.data.db.datasources.SecretsDatabaseDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PassModule {

    @Provides
    @Singleton
    fun provideSecretsDatabaseDataSource(database: PassDatabase) =
        SecretsDatabaseDataSource(database)
    
}
