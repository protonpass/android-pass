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

package proton.android.pass.enterextrapassword

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import proton.android.pass.ForegroundActivityTracker
import proton.android.pass.domain.UserId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class EnterExtraPasswordLauncher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val foregroundActivityTracker: ForegroundActivityTracker
) {

    fun start(userId: UserId) {
        val intent = Intent(context, EnterExtraPasswordActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(EnterExtraPasswordActivity.EXTRA_USER_ID, userId.id)
        }
        val activity = foregroundActivityTracker.launchSourceActivity
        if (activity != null) {
            activity.startActivity(intent)
        } else {
            PassLogger.i(TAG, "No activity to launch from, starting extra password in a new task")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private companion object {
        private const val TAG = "EnterExtraPasswordLauncher"
    }
}
