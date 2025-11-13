package proton.android.pass.inappreview.impl

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.inappreview.api.InAppReviewManager
import javax.inject.Inject

class InAppReviewManagerFdroidImpl @Inject constructor() : InAppReviewManager {
    override fun shouldRequestReview(): Flow<Boolean> = flowOf(false)

    override fun requestReview(activityHolder: ClassHolder<Activity>) {

    }
}
