/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.data.impl.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.usecases.passwordHistoryEntry.DeleteOldPasswordHistoryEntry
import proton.android.pass.log.api.PassLogger
import java.util.concurrent.TimeUnit

@HiltWorker
class ClearPasswordHistoryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val deleteOldPasswordHistoryEntry: DeleteOldPasswordHistoryEntry
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = safeRunCatching {
        deleteOldPasswordHistoryEntry()
    }.onSuccess {
        PassLogger.i(TAG, "Finished $TAG")
    }.onFailure {
        PassLogger.w(TAG, "Failed clear password history")
        PassLogger.w(TAG, it)
    }.toWorkerResult()


    companion object {
        private const val TAG = "ClearPasswordHistoryWorker"

        const val WORKER_UNIQUE_NAME = "clear_password_history_worker"

        private const val REPEAT_PERIOD = 1L

        fun getRequestFor(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<ClearPasswordHistoryWorker>(REPEAT_PERIOD, TimeUnit.DAYS)
                .build()
    }
}
