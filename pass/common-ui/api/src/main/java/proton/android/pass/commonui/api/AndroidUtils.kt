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
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.log.api.PassLogger

object AndroidUtils {

    const val TAG = "AndroidUtils"

    @Suppress("DEPRECATION")
    fun getApplicationName(context: Context, packageName: String): Option<String> =
        try {
            val packageManager = context.packageManager
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                )
            } else {
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            }
            packageManager.getApplicationLabel(appInfo).toString().toOption()
        } catch (e: PackageManager.NameNotFoundException) {
            PassLogger.d(TAG, e, "Package name not found")
            None
        }

    fun getApplicationIcon(context: Context, packageName: String): Option<Drawable> =
        try {
            context.packageManager.getApplicationIcon(packageName).toOption()
        } catch (e: PackageManager.NameNotFoundException) {
            PassLogger.d(TAG, e, "Package name not found")
            None
        }

}
