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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.log.api.PassLogger

object AndroidUtils {

    private const val TAG = "AndroidUtils"

    @Suppress("DEPRECATION")
    fun getApplicationName(context: Context, packageName: String): Option<String> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val hasPermission = context.checkSelfPermission(
                android.Manifest.permission.QUERY_ALL_PACKAGES
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                PassLogger.i(TAG, "QUERY_ALL_PACKAGES permission not granted, returning None")
                return None
            }
        }

        return runCatching {
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
        }.getOrElse { throwable ->
            PassLogger.w(TAG, throwable)
            PassLogger.w(TAG, "Failed to get application name")
            None
        }
    }

    fun getApplicationIcon(context: Context, packageName: String): Option<Drawable> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val hasPermission = context.checkSelfPermission(
                android.Manifest.permission.QUERY_ALL_PACKAGES
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                PassLogger.i(TAG, "QUERY_ALL_PACKAGES permission not granted, returning None")
                return None
            }
        }

        return runCatching {
            context.packageManager.getApplicationIcon(packageName).toOption()
        }.getOrElse { throwable ->
            PassLogger.w(TAG, throwable)
            PassLogger.w(TAG, "Failed to get application icon")
            None
        }
    }

    fun shareTextWithThirdParties(
        context: Context,
        text: String,
        title: String? = null
    ) {
        try {
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }.let { sendIntent ->
                Intent.createChooser(sendIntent, title)
            }.also { shareIntent ->
                context.startActivity(shareIntent)
            }
        } catch (exception: ActivityNotFoundException) {
            PassLogger.w(TAG, "Error sharing text with third parties")
            PassLogger.w(TAG, exception)
        }
    }

}
