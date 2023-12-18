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

package proton.android.pass.data.impl.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import proton.android.pass.data.impl.usecases.SendUserAccessRequest
import proton.android.pass.data.impl.usecases.SendUserAccessResult
import proton.android.pass.log.api.PassLogger

@HiltWorker
class UserAccessWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val sendUserAccessRequest: SendUserAccessRequest
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        PassLogger.i(TAG, "Starting $TAG attempt $runAttemptCount")
        return when (sendUserAccessRequest()) {
            SendUserAccessResult.Retry -> Result.retry()
            SendUserAccessResult.Failure -> Result.failure()
            SendUserAccessResult.Success -> Result.success()
        }
    }


    companion object {
        const val WORKER_UNIQUE_NAME = "user_access_worker"
        private const val TAG = "UserAccessWorker"
    }
}
