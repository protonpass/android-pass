/*
 * Copyright (c) 2025-2026 Proton AG
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

package proton.android.pass.autofill

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

enum class ThirdPartyMode {
    Enabled,
    Disabled,
    Unknown
}

interface ThirdPartyModeProvider {
    fun isThirdPartyModeEnabled(browserPackage: String): Boolean
    fun getThirdPartyMode(browserPackage: String): ThirdPartyMode
}

@Singleton
class ThirdPartyModeProviderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ThirdPartyModeProvider {

    override fun isThirdPartyModeEnabled(browserPackage: String): Boolean =
        getThirdPartyMode(browserPackage) == ThirdPartyMode.Enabled

    @Suppress("ReturnCount")
    override fun getThirdPartyMode(browserPackage: String): ThirdPartyMode {
        val hasPermission = context.checkSelfPermission(
            "android.permission.READ_USER_DICTIONARY"
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            PassLogger.d(
                TAG,
                "READ_USER_DICTIONARY permission not granted, third-party mode is unknown"
            )
            return ThirdPartyMode.Unknown
        }

        return runCatching {
            val authority = browserPackage + CONTENT_PROVIDER_SUFFIX
            val uri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority)
                .path(URI_PATH)
                .build()

            val projection = arrayOf(COLUMN_STATE)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)

            if (cursor == null) {
                PassLogger.d(TAG, "ContentProvider unavailable: $authority")
                return ThirdPartyMode.Unknown
            }

            cursor.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(COLUMN_STATE)
                    if (index == -1) {
                        PassLogger.d(TAG, "Column '$COLUMN_STATE' not found in cursor")
                        return ThirdPartyMode.Unknown
                    }
                    if (it.getInt(index) != 0) ThirdPartyMode.Enabled else ThirdPartyMode.Disabled
                } else {
                    PassLogger.d(TAG, "Empty cursor when querying $uri")
                    ThirdPartyMode.Unknown
                }
            }
        }.getOrElse { throwable ->
            PassLogger.w(TAG, throwable)
            PassLogger.w(TAG, "Failed to query third-party mode from $browserPackage")
            ThirdPartyMode.Unknown
        }
    }

    companion object {
        private const val TAG = "ThirdPartyModeProvider"
        private const val CONTENT_PROVIDER_SUFFIX = ".AutofillThirdPartyModeContentProvider"
        private const val COLUMN_STATE = "autofill_third_party_state"
        private const val URI_PATH = "autofill_third_party_mode"
    }
}
