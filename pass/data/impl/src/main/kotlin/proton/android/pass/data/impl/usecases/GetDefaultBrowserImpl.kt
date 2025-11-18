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

package proton.android.pass.data.impl.usecases

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import proton.android.pass.data.api.usecases.DefaultBrowser
import proton.android.pass.data.api.usecases.GetDefaultBrowser
import javax.inject.Inject

class GetDefaultBrowserImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : GetDefaultBrowser {

    @Suppress("Deprecation")
    override fun invoke(): DefaultBrowser {
        val browserIntent = Intent("android.intent.action.VIEW", Uri.parse("http://"))

        val resolveInfo: ResolveInfo = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.resolveActivity(
                    browserIntent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                context.packageManager.resolveActivity(
                    browserIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
            } ?: return DefaultBrowser.Other
        }.getOrElse {
            return DefaultBrowser.Other
        }

        val packageName = resolveInfo.activityInfo.packageName
        return if (SAMSUNG_BROWSER_PACKAGE_NAMES.contains(packageName)) {
            DefaultBrowser.Samsung
        } else {
            DefaultBrowser.Other
        }
    }

    companion object {
        private val SAMSUNG_BROWSER_PACKAGE_NAMES = listOf(
            "com.sec.android.app.sbrowser.beta",
            "com.sec.android.app.sbrowser"
        )
    }
}
