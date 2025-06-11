/*
 * Copyright (c) 2025 Proton AG
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
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface ThirdPartyModeProvider {
    fun isThirdPartyModeEnabled(browserPackage: String): Boolean
}

@Singleton
class ThirdPartyModeProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ThirdPartyModeProvider {

    override fun isThirdPartyModeEnabled(browserPackage: String): Boolean {
        val authority = browserPackage + CONTENT_PROVIDER_SUFFIX
        val uri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(authority)
            .path(URI_PATH)
            .build()

        val projection = arrayOf(COLUMN_STATE)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
            ?: throw UnsupportedOperationException("ContentProvider unavailable: $authority")

        cursor.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(COLUMN_STATE)
                if (index == -1) {
                    throw IllegalStateException("Column '$COLUMN_STATE' not found in cursor")
                }
                return it.getInt(index) != 0
            } else {
                throw IllegalStateException("Empty cursor when querying $uri")
            }
        }
    }

    companion object {
        private const val CONTENT_PROVIDER_SUFFIX = ".AutofillThirdPartyModeContentProvider"
        private const val COLUMN_STATE = "autofill_third_party_state"
        private const val URI_PATH = "autofill_third_party_mode"
    }
}
