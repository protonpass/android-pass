package proton.android.pass.preferences

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesBinderModule {
    @Binds
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds
    abstract fun bindFeatureFlagsPreferencesRepository(
        impl: FeatureFlagsPreferencesRepositoryImpl
    ): FeatureFlagsPreferencesRepository

    @Binds
    abstract fun bindInternalSettingsRepository(
        impl: InternalSettingsRepositoryImpl
    ): InternalSettingsRepository
}
