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

package proton.android.pass.data.impl.migration

import kotlinx.coroutines.delay
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.impl.db.AppDatabase
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.local.LocalDataMigrationDataSource
import proton.android.pass.data.impl.migration.itemhaspasskey.ItemHasPasskeyMigrator
import proton.android.pass.data.impl.migration.itemhastotp.ItemHasTotpMigrator
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataMigratorImpl @Inject constructor(
    private val database: PassDatabase,
    private val appDatabase: AppDatabase,
    private val migrationDataSource: LocalDataMigrationDataSource,
    itemHasTotpMigrator: ItemHasTotpMigrator,
    itemHasPasskeyMigrator: ItemHasPasskeyMigrator
) : DataMigrator {

    private val allMigrations: List<Migrator> = listOf(
        itemHasTotpMigrator,
        itemHasPasskeyMigrator
    )

    override suspend fun areMigrationsNeeded(): Boolean {
        if (!waitForRoomToBeInitialized()) {
            PassLogger.w(
                TAG,
                "Room is not initialized yet, we can't check if migrations are needed"
            )
            return true
        }
        val migrationsToExecute = getPendingMigrations()
        return migrationsToExecute.isNotEmpty()
    }

    override suspend fun run(): Result<Unit> = safeRunCatching { runMigrations() }

    private suspend fun runMigrations() {
        if (!waitForRoomToBeInitialized()) {
            PassLogger.w(TAG, "Room is not initialized yet, we can't perform any operation")
            return
        }
        getPendingMigrations()
            .forEach { migrator ->
                database.inTransaction("getPendingMigrations") {
                    PassLogger.i(TAG, "Running migration ${migrator.migrationName}")
                    migrator.migrate()
                    PassLogger.i(TAG, "Successfully run migration ${migrator.migrationName}")
                    migrationDataSource.markMigrationAsExecuted(migrator.migrationName)
                }
            }
    }

    private suspend fun getPendingMigrations(): List<Migrator> {
        val executedMigrations = migrationDataSource.getExecutedMigrations().map { it.name }
        val allMigrationNames = allMigrations.associateBy { it.migrationName }

        return allMigrations
            .filterNot { migration -> executedMigrations.contains(migration.migrationName) }
            .mapNotNull { allMigrationNames[it.migrationName] }
    }

    private suspend fun waitForRoomToBeInitialized(): Boolean {
        var retryDelay = DATABASE_OPEN_RETRY_DELAY
        for (i in 0..MAX_DATABASE_OPEN_RETRIES) {
            // Once this call finishes we can be sure that Room is initialized
            val database = appDatabase.openHelper.readableDatabase
            if (database.isOpen) {
                return true
            }
            delay(retryDelay)
            retryDelay *= 2
        }
        return false
    }

    companion object {
        private const val TAG = "DataMigratorImpl"

        private const val MAX_DATABASE_OPEN_RETRIES = 10
        private const val DATABASE_OPEN_RETRY_DELAY = 100L
    }

}
