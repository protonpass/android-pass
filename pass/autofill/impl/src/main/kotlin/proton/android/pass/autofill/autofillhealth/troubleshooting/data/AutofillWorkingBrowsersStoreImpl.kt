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

package proton.android.pass.autofill.autofillhealth.troubleshooting.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AutofillWorkingBrowsersStoreImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AutofillWorkingBrowsersStore {

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val browserPackages: Set<String> by lazy { resolveBrowserPackages() }

    override fun recordWorkingBrowser(packageName: String) {
        if (packageName.isBlank() || packageName !in browserPackages) return
        val current = preferences.getStringSet(KEY_WORKING_BROWSERS, emptySet()).orEmpty()
        if (packageName !in current) {
            preferences.edit()
                .putStringSet(KEY_WORKING_BROWSERS, current + packageName)
                .apply()
        }
    }

    override fun getWorkingBrowsers(): Set<String> =
        preferences.getStringSet(KEY_WORKING_BROWSERS, emptySet()).orEmpty().toSet()

    private fun resolveBrowserPackages(): Set<String> {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PROBE_URL))
            .addCategory(Intent.CATEGORY_BROWSABLE)
        return context.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .mapNotNull { resolveInfo -> resolveInfo.activityInfo?.packageName }
            .toSet()
    }

    private companion object {
        private const val PREFS_NAME = "autofill_working_browsers"
        private const val KEY_WORKING_BROWSERS = "working_browsers"
        private const val PROBE_URL = "https://example.com"
    }
}
