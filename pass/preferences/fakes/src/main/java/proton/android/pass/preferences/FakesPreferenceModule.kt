package proton.android.pass.preferences

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FakesPreferenceModule {

    @Binds
    abstract fun bindUserPreferencesRepository(impl: TestPreferenceRepository): UserPreferencesRepository

    @Binds
    abstract fun bindFeatureFlagsPreferencesRepository(
        impl: TestFeatureFlagsPreferenceRepository
    ): FeatureFlagsPreferencesRepository

    @Binds
    abstract fun bindInternalSettingsRepository(
        impl: TestInternalSettingsRepository
    ): InternalSettingsRepository
}
