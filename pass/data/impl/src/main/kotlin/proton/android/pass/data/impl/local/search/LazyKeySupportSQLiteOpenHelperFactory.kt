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

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.log.api.PassLogger

internal class LazyKeySupportSQLiteOpenHelperFactory(
    private val keyProvider: SearchDatabaseKeyProvider,
    private val appDispatchers: AppDispatchers,
    private val delegateFactoryFor: (SearchDatabaseKey) -> SupportSQLiteOpenHelper.Factory = ::sqlCipherFactoryFor
) : SupportSQLiteOpenHelper.Factory {

    override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper =
        LazyKeySupportSQLiteOpenHelper(configuration, keyProvider, appDispatchers, delegateFactoryFor)
}

private class LazyKeySupportSQLiteOpenHelper(
    private val configuration: SupportSQLiteOpenHelper.Configuration,
    private val keyProvider: SearchDatabaseKeyProvider,
    private val appDispatchers: AppDispatchers,
    private val delegateFactoryFor: (SearchDatabaseKey) -> SupportSQLiteOpenHelper.Factory
) : SupportSQLiteOpenHelper {

    private val lock = Any()
    private var delegate: SupportSQLiteOpenHelper? = null
    private var writeAheadLoggingEnabled: Boolean? = null

    override val databaseName: String? = configuration.name

    override val writableDatabase: SupportSQLiteDatabase
        get() = resolveDelegate().writableDatabase

    override val readableDatabase: SupportSQLiteDatabase
        get() = resolveDelegate().readableDatabase

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        synchronized(lock) {
            writeAheadLoggingEnabled = enabled
            delegate?.setWriteAheadLoggingEnabled(enabled)
        }
    }

    override fun close() {
        synchronized(lock) {
            delegate?.close()
        }
    }

    private fun resolveDelegate(): SupportSQLiteOpenHelper = synchronized(lock) {
        delegate ?: createDelegate().also { helper ->
            writeAheadLoggingEnabled?.let(helper::setWriteAheadLoggingEnabled)
            delegate = helper
        }
    }

    private fun createDelegate(): SupportSQLiteOpenHelper {
        val key = resolveKey()
        val delegateConfiguration = when (key) {
            is SearchDatabaseKey.Persistent -> {
                PassLogger.i(TAG, "Creating SearchDatabase with SQLCipher encryption")
                configuration
            }

            SearchDatabaseKey.Unavailable -> {
                PassLogger.w(TAG, "Search database key unavailable, using in-memory SearchDatabase")
                configuration.inMemory()
            }
        }
        return delegateFactoryFor(key).create(delegateConfiguration)
    }

    private fun resolveKey(): SearchDatabaseKey = runCatching {
        runBlocking(appDispatchers.io) { keyProvider.getOrCreateKey() }
    }.getOrElse { error ->
        PassLogger.w(TAG, "Error resolving search database key")
        PassLogger.w(TAG, error)
        SearchDatabaseKey.Unavailable
    }

    private fun SupportSQLiteOpenHelper.Configuration.inMemory() = SupportSQLiteOpenHelper.Configuration
        .builder(context)
        .name(null)
        .callback(callback)
        .noBackupDirectory(useNoBackupDirectory)
        .allowDataLossOnRecovery(allowDataLossOnRecovery)
        .build()

    private companion object {
        private const val TAG = "LazyKeySupportSQLiteOpenHelper"
    }
}

private fun sqlCipherFactoryFor(key: SearchDatabaseKey): SupportSQLiteOpenHelper.Factory = when (key) {
    is SearchDatabaseKey.Persistent -> SupportOpenHelperFactory(key.passphrase)
    SearchDatabaseKey.Unavailable -> SupportOpenHelperFactory(ByteArray(0))
}
