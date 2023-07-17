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
import proton.android.pass.inappreview.api.InAppReviewManager
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class InAppReviewManagerImpl @Inject constructor() : InAppReviewManager {

    override fun requestReview(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow()
            .addOnSuccessListener { reviewInfo ->
                manager.launchReviewFlow(activity, reviewInfo)
            }
            .addOnFailureListener { exception ->
                @ReviewErrorCode val reviewErrorCode = (exception as ReviewException).errorCode
                PassLogger.w(TAG, "reviewErrorCode=$reviewErrorCode")
            }
    }

    companion object {
        private const val TAG = "InAppReviewManagerImpl"
    }
}
