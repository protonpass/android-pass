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

package proton.android.pass.network.impl

import android.os.Build
import me.proton.core.network.domain.ApiClient
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class PassApiClient @Inject constructor(appConfig: AppConfig) : ApiClient {
    override val appVersionHeader: String = "android-pass@${appConfig.versionName}"
    override val enableDebugLogging: Boolean = appConfig.isDebug
    override val userAgent: String = StringBuilder()
        .append("ProtonPass/${appConfig.versionName}")
        .append(" ")
        .append("(")
        .append("Android ${Build.VERSION.RELEASE};")
        .append(" ")
        .append("${Build.BRAND} ${Build.MODEL}")
        .append(")")
        .toString()

    override suspend fun shouldUseDoh(): Boolean = false
    override fun forceUpdate(errorMessage: String) {
        PassLogger.i(TAG, errorMessage)
    }

    companion object {
        const val TAG = "PassApiClient"
    }
}
