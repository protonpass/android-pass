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

package proton.android.pass.inappreview.impl

import android.app.Activity
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.inappreview.api.InAppReviewManager
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.InternalSettingsRepository
import javax.inject.Inject

class InAppReviewManagerImpl @Inject constructor(
    private val internalSettingsRepository: InternalSettingsRepository
) : InAppReviewManager {

    override fun shouldRequestReview(): Flow<Boolean> = combine(
        internalSettingsRepository.getItemCreateCount(),
        internalSettingsRepository.getInAppReviewTriggered()
    ) { itemCreateCount, inAppReviewTriggered ->
        when {
            !inAppReviewTriggered -> itemCreateCount == ITEM_CREATED_TRIGGER
            else -> false
        }
    }

    override fun requestReview(activityHolder: ClassHolder<Activity>) {
        val activity = activityHolder.get().value() ?: return
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow()
            .addOnSuccessListener { reviewInfo ->
                manager.launchReviewFlow(activity, reviewInfo)
                    .addOnFailureListener { exception ->
                        PassLogger.w(TAG, exception, "Review flow failed")
                    }
                    .addOnSuccessListener {
                        PassLogger.i(TAG, "Review flow completed")
                    }
            }
            .addOnFailureListener { exception ->
                @ReviewErrorCode val reviewErrorCode = (exception as ReviewException).errorCode
                PassLogger.w(TAG, exception, "reviewErrorCode=$reviewErrorCode")
            }
            .addOnCompleteListener {
                internalSettingsRepository.setInAppReviewTriggered(true)
            }
    }

    companion object {
        private const val TAG = "InAppReviewManagerImpl"
        private const val ITEM_CREATED_TRIGGER = 10
    }
}
