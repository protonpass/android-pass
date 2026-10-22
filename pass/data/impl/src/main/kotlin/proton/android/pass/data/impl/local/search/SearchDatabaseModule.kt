/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.local.search

import android.content.Context
import androidx.room.InvalidationTracker
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import proton.android.pass.common.api.AppDispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SearchDatabaseModule {

    init {
        System.loadLibrary("sqlcipher")
    }

    @Provides
    @Singleton
    fun provideSearchDatabase(
        @ApplicationContext context: Context,
        keyProvider: SearchDatabaseKeyProvider,
        appDispatchers: AppDispatchers
    ): SearchDatabase = buildSearchDatabase(context, keyProvider, appDispatchers)

    @Provides
    fun provideSearchDao(database: SearchDatabase): SearchDao = database.searchDao()

    @Provides
    fun provideSearchInvalidationTracker(database: SearchDatabase): InvalidationTracker = database.invalidationTracker
}


@Module
@InstallIn(SingletonComponent::class)
abstract class SearchDatabaseBindsModule {

    @Binds
    @Singleton
    abstract fun bindSearchDatabaseKeyProvider(impl: SearchDatabaseKeyProviderImpl): SearchDatabaseKeyProvider
}

internal fun buildSearchDatabase(
    context: Context,
    keyProvider: SearchDatabaseKeyProvider,
    appDispatchers: AppDispatchers,
    name: String = SearchDatabase.DB_NAME
): SearchDatabase = Room.databaseBuilder(context, SearchDatabase::class.java, name)
    .openHelperFactory(LazyKeySupportSQLiteOpenHelperFactory(keyProvider, appDispatchers))
    .fallbackToDestructiveMigration()
    .addCallback(SearchFtsCallback)
    .build()
