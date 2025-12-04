/*
 * Copyright (c) 2024 Proton AG
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
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.impl.usecases.assetlink.UpdateAssetLink
import proton.android.pass.log.api.PassLogger

@HiltWorker
class SingleItemAssetLinkWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val updateAssetLink: UpdateAssetLink
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result = safeRunCatching {
        PassLogger.i(TAG, "Starting $TAG attempt $runAttemptCount")
        val inputWebsites = workerParameters.inputData.getStringArray(WEBSITES_KEY)
            ?: return Result.failure()
        if (inputWebsites.isNotEmpty()) {
            updateAssetLink(inputWebsites.toSet())
        }
    }
        .onFailure {
            PassLogger.w(TAG, "Failed to refresh asset links")
            PassLogger.w(TAG, it)
        }
        .toWorkerResult()

    companion object {
        private const val WEBSITES_KEY = "WEBSITES"
        private const val TAG = "SingleItemAssetLinkWorker"

        fun getRequestFor(websites: Set<String>): OneTimeWorkRequest {
            val inputData = workDataOf(WEBSITES_KEY to websites.toTypedArray())
            return OneTimeWorkRequestBuilder<SingleItemAssetLinkWorker>()
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .setInputData(inputData)
                .build()
        }
    }
}
