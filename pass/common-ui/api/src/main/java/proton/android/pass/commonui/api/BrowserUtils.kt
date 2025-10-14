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

package proton.android.pass.commonui.api

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import proton.android.pass.log.api.PassLogger

object BrowserUtils {
    const val TAG = "BrowserUtils"

    // Test-friendly variable
    @Volatile
    var lastAttemptedUrl: String? = null
        private set

    @Volatile
    var wasCalled: Boolean = false
        private set

    fun openWebsite(context: Context, website: String) {
        // Track the attempt for testing
        lastAttemptedUrl = website
        wasCalled = true

        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, website.toUri())
            if (intent.resolveActivity(context.packageManager) != null) {
                val chooser = Intent.createChooser(intent, context.getString(R.string.browser_open_with))
                context.startActivity(chooser)
            } else {
                PassLogger.w(TAG, "No application can handle this URL")
            }
        }.onFailure {
            PassLogger.w(TAG, "Could not find a suitable activity")
            PassLogger.w(TAG, it)
        }
    }

    // Test helper to reset tracking
    fun resetLastUrl() {
        lastAttemptedUrl = null
        wasCalled = false
    }
}
