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

package proton.android.pass.data.impl.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseCleanupHelper @Inject constructor(
    private val database: AppDatabase
) {

    suspend fun cleanupUserData() = withContext(Dispatchers.IO) {
        PassLogger.i(TAG, "Starting per-user cleanup")

        safeRunCatching {
            val db = database.openHelper.writableDatabase

            db.query("PRAGMA wal_checkpoint(PASSIVE)").close()
            PassLogger.d(TAG, "Performed passive WAL checkpoint")

            PassLogger.i(TAG, "Per-user cleanup completed successfully")
        }.onFailure { e ->
            PassLogger.w(TAG, "Error during per-user cleanup")
            PassLogger.w(TAG, e)
        }
    }

    suspend fun cleanupLastUser() = withContext(Dispatchers.IO) {
        PassLogger.i(TAG, "Starting last-user cleanup")

        safeRunCatching {
            val db = database.openHelper.writableDatabase
            safeRunCatching {
                val deletedCount = db.compileStatement(
                    "DELETE FROM SessionEntity WHERE userId IS NULL OR userId = ''"
                ).use { it.executeUpdateDelete() }
                PassLogger.d(TAG, "Cleaned up $deletedCount orphaned sessions")
            }.onFailure { e ->
                PassLogger.w(TAG, "Could not clean sessions, may have foreign keys")
                PassLogger.w(TAG, e)
            }

            safeRunCatching {
                db.query("PRAGMA wal_checkpoint(TRUNCATE)").close()
                PassLogger.i(TAG, "Performed WAL checkpoint with truncate")
            }.onFailure { e ->
                PassLogger.w(TAG, "WAL checkpoint failed")
                PassLogger.w(TAG, e)
            }

            PassLogger.i(TAG, "Last-user cleanup completed")
        }.onFailure { e ->
            PassLogger.w(TAG, "Error during last-user cleanup")
            PassLogger.w(TAG, e)
        }
    }

    companion object {
        private const val TAG = "DatabaseCleanupHelper"
    }
}
