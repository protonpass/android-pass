package proton.android.pass.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import proton.android.pass.preferences.UserPreferencesRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface UserPreferenceModule {
    fun getRepository(): UserPreferencesRepository
}

