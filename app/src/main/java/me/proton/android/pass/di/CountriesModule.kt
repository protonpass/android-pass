package me.proton.android.pass.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.proton.core.country.data.repository.CountriesRepositoryImpl
import me.proton.core.country.domain.repository.CountriesRepository

@Module
@InstallIn(SingletonComponent::class)
class CountriesModule {

    @Provides
    fun provideCountriesRepository(@ApplicationContext context: Context): CountriesRepository =
        CountriesRepositoryImpl(context)
}
