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

package proton.android.pass.protonapps.impl

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor
import proton.android.pass.log.api.PassLogger
import proton.android.pass.protonapps.api.ProtonApp
import proton.android.pass.protonapps.api.usecases.OpenProtonApp
import proton.android.pass.protonapps.api.usecases.OpenResult
import javax.inject.Inject

internal class OpenProtonAppImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appConfig: AppConfig
) : OpenProtonApp {

    override fun invoke(app: ProtonApp): OpenResult {
        if (app.isInstalled) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.type.id)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return OpenResult.OpenedApp
            }
        }
        return when (appConfig.flavor) {
            is BuildFlavor.Fdroid -> openWeb(app.type.fdroidFallbackUrl!!)
            else -> openPlayStore(app.type.id, app.type.playStoreFallbackUrl)
        }
    }

    private fun openPlayStore(packageName: String, webFallback: String): OpenResult {
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        marketIntent.setPackage(PLAY_STORE_PACKAGE)
        marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(marketIntent)
            OpenResult.OpenedPlayStore
        } catch (_: ActivityNotFoundException) {
            PassLogger.w(TAG, "Play Store app unavailable, falling back to web URL")
            openWeb(webFallback)
        }
    }

    private fun openWeb(url: String): OpenResult {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(webIntent)
        return OpenResult.OpenedWebFallback
    }

    private companion object {
        private const val TAG = "OpenProtonAppImpl"
        private const val PLAY_STORE_PACKAGE = "com.android.vending"
    }
}
