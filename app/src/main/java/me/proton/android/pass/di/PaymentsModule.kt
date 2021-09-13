package me.proton.android.pass.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.network.data.ApiProvider
import me.proton.core.payment.data.repository.PaymentsRepositoryImpl
import me.proton.core.payment.domain.repository.PaymentsRepository
import me.proton.core.payment.presentation.entity.SecureEndpoint
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PaymentsModule {

    @Provides
    @Singleton
    fun providePaymentsRepository(apiProvider: ApiProvider): PaymentsRepository =
        PaymentsRepositoryImpl(apiProvider)


    @Provides
    @Singleton
    fun provideSecureEndpoint(): SecureEndpoint = SecureEndpoint("secure.protonmail.com")
}
