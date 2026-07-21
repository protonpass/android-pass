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

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CurrentAutofillServiceProviderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : CurrentAutofillServiceProvider {

    override fun getActiveOtherServiceLabel(): String? {
        val flattened = runCatching {
            Settings.Secure.getString(context.contentResolver, AUTOFILL_SERVICE_KEY)
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: return null

        val packageName = ComponentName.unflattenFromString(flattened)?.packageName ?: return null
        if (packageName == context.packageName) return null

        return runCatching {
            val packageManager = context.packageManager
            packageManager
                .getApplicationLabel(packageManager.getApplicationInfo(packageName, 0))
                .toString()
        }.getOrNull()
    }

    private companion object {
        private const val AUTOFILL_SERVICE_KEY = "autofill_service"
    }
}
