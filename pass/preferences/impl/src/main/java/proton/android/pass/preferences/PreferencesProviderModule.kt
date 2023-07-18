/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

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
            serializer = UserPreferencesSerializer,
            migrations = listOf(UserPreferenceMigration.MIGRATION_1)
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
