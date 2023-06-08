package proton.android.pass.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesProviderModule {

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<UserPreferences> =
        DataStoreFactory.create(
            serializer = UserPreferencesSerializer
        ) {
            context.dataStoreFile("user_preferences.pb")
        }

    @Provides
    @Singleton
    fun provideFFDataStore(
        @ApplicationContext context: Context
    ): DataStore<FeatureFlagsPreferences> =
        DataStoreFactory.create(
            serializer = FeatureFlagsPreferencesSerializer
        ) {
            context.dataStoreFile("feature_flag_preferences.pb")
        }


    @Provides
    @Singleton
    fun provideInternalSettingsDataStore(
        @ApplicationContext context: Context
    ): DataStore<InternalSettings> =
        DataStoreFactory.create(
            serializer = InternalSettingsSerializer
        ) {
            context.dataStoreFile("internal_settings.pb")
        }
}
