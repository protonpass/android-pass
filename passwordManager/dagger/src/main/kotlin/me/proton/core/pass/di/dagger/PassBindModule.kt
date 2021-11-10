package me.proton.core.pass.di.dagger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.pass.data.repositories.SecretsRepositoryImpl
import me.proton.core.pass.domain.repositories.SecretsRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class PassBindModule {

    @Binds
    abstract fun bindSecretsRepository(repository: SecretsRepositoryImpl): SecretsRepository

}
