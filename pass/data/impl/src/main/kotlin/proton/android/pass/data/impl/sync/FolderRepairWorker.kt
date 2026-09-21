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

package proton.android.pass.data.impl.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.common.api.transpose
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.usecases.sync.CheckFolderForceSync
import proton.android.pass.log.api.LogAccountContext
import proton.android.pass.log.api.PassLogger

/**
 * Runs the folders repair right after an app update, so autofill and autosave do not read stale
 * folders while waiting for the next [SyncWorker] tick.
 */
@HiltWorker
open class FolderRepairWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val checkFolderForceSync: CheckFolderForceSync,
    private val accountManager: AccountManager
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        PassLogger.i(TAG, "Starting $TAG attempt $runAttemptCount")

        val result = accountManager.getAccounts(AccountState.Ready)
            .first()
            .map { account ->
                withContext(LogAccountContext(account.userId)) {
                    safeRunCatching {
                        checkFolderForceSync(account.userId, SyncMode.Background)
                    }
                        .onSuccess { PassLogger.i(TAG, "Folder repair check finished") }
                        .onFailure { error ->
                            PassLogger.w(TAG, "Folder repair check finished with error")
                            PassLogger.w(TAG, error)
                        }
                }
            }
            .transpose()

        return if (result.isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {

        private const val TAG = "FolderRepairWorker"

        const val WORKER_UNIQUE_NAME = "folder_repair_worker"

        val EXISTING_WORK_POLICY = ExistingWorkPolicy.KEEP

        fun getRequest(): OneTimeWorkRequest = OneTimeWorkRequestBuilder<FolderRepairWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .build()
    }
}
