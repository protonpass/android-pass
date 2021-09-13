package me.proton.android.pass.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.ClientSecret
import me.proton.core.domain.entity.Product
import me.proton.core.util.kotlin.Logger
import me.proton.android.pass.log.PassKeyLogger
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun provideLogger(): Logger = PassKeyLogger()

    @Provides
    @Singleton
    fun provideProduct(): Product =
        Product.Drive

    @Provides
    @ClientSecret
    fun provideClientSecret(): String = ""

    @Provides
    @Singleton
    fun provideRequiredAccountType(): AccountType =
        AccountType.Internal
}
