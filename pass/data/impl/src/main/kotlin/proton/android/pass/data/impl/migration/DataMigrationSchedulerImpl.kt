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

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.runBlocking
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataMigrationSchedulerImpl @Inject constructor(
    private val workManager: WorkManager,
    private val dataMigrator: DataMigrator
) : DataMigrationScheduler {

    override fun schedule() {

        val areMigrationsNeeded = runBlocking { dataMigrator.areMigrationsNeeded() }
        if (!areMigrationsNeeded) {
            PassLogger.d(TAG, "Not scheduling DataMigrationWorker, no migrations needed")
        } else {
            PassLogger.d(TAG, "Scheduling DataMigrationWorker, migrations needed")
            val request = OneTimeWorkRequestBuilder<DataMigrationWorker>().build()
            workManager.enqueue(request)
        }
    }

    companion object {
        private const val TAG = "DataMigrationSchedulerImpl"
    }

}
