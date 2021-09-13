package me.proton.android.pass.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.network.data.ApiProvider
import me.proton.core.plan.data.repository.PlansRepositoryImpl
import me.proton.core.plan.domain.SupportedPaidPlanIds
import me.proton.core.plan.domain.repository.PlansRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlansModule {

    @Provides
    @SupportedPaidPlanIds
    fun provideClientSupportedPaidPlanIds(): List<String> =
        listOf("ziWi-ZOb28XR4sCGFCEpqQbd1FITVWYfTfKYUmV_wKKR3GsveN4HZCh9er5dhelYylEp-fhjBbUPDMHGU699fw==")

    @Provides
    @Singleton
    fun providePlansRepository(apiProvider: ApiProvider): PlansRepository =
        PlansRepositoryImpl(apiProvider)
}
