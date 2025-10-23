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
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.inappmessages.ChangeInAppMessageStatus
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import proton.android.pass.log.api.PassLogger

@HiltWorker
class ChangeInAppMessageStatusWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val changeInAppMessageStatus: ChangeInAppMessageStatus
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result = runCatching {
        PassLogger.i(TAG, "Starting $TAG attempt $runAttemptCount")
        val userId = workerParameters.inputData.getString(USER_ID_KEY)
            ?.let(::UserId)
        val inAppMessageId = workerParameters.inputData.getString(IN_APP_MESSAGES_KEY)
            ?.let(::InAppMessageId)
        val status = workerParameters.inputData.getInt(IN_APP_MESSAGE_STATUS_KEY, -1)
            .let { InAppMessageStatus.fromValue(it) }
        if (userId != null && inAppMessageId != null && status != InAppMessageStatus.Unknown) {
            changeInAppMessageStatus(userId, inAppMessageId, status)
        } else {
            PassLogger.w(TAG, "Failed to get userId or inAppMessageId")
            return Result.failure()
        }
    }
        .onSuccess {
            PassLogger.i(TAG, "Successfully marked in-app message as dismissed")
        }
        .onFailure {
            PassLogger.w(TAG, "Failed to mark in-app message as dismissed")
            PassLogger.w(TAG, it)
        }
        .toWorkerResult()

    companion object Companion {
        private const val USER_ID_KEY = "USER_ID"
        private const val IN_APP_MESSAGES_KEY = "IN_APP_MESSAGES"
        private const val IN_APP_MESSAGE_STATUS_KEY = "IN_APP_MESSAGE_STATUS"
        private const val TAG = "ChangeInAppMessageStatusWorker"

        fun getRequestFor(
            userId: UserId,
            inAppMessageId: InAppMessageId,
            inAppMessageStatus: InAppMessageStatus
        ): OneTimeWorkRequest {
            val inputData = workDataOf(
                USER_ID_KEY to userId.id,
                IN_APP_MESSAGES_KEY to inAppMessageId.value,
                IN_APP_MESSAGE_STATUS_KEY to inAppMessageStatus.value
            )
            return OneTimeWorkRequestBuilder<ChangeInAppMessageStatusWorker>()
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .setInputData(inputData)
                .build()
        }
    }
}
