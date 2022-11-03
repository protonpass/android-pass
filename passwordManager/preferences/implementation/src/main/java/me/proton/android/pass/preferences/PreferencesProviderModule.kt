package me.proton.android.pass.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.pass.preferences.extensions.dataStore

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesProviderModule {

    @Provides
    fun provideDataStore(context: Context): DataStore<Preferences> = context.dataStore
}
