package me.proton.android.pass.preferences

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesPreferenceModule {

    @Binds
    abstract fun bindUserPreferencesRepository(impl: TestPreferenceRepository): UserPreferencesRepository
}
