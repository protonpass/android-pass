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

package proton.android.pass.commonui.api

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import proton.android.pass.log.api.PassLogger

object EmailUtils {
    private const val TAG = "EmailUtils"

    fun sendEmail(context: Context, email: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:${Uri.encode(email)}".toUri()
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                PassLogger.w(TAG, "No email application found")
            }
        }.onFailure {
            PassLogger.w(TAG, "Could not launch email intent")
            PassLogger.w(TAG, it)
        }
    }
}
