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

package proton.android.pass

import android.os.Build
import me.proton.core.configuration.EnvironmentConfiguration
import me.proton.core.util.kotlin.takeIfNotBlank
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor
import javax.inject.Inject

class PassAppConfig @Inject constructor(
    environmentConfiguration: EnvironmentConfiguration
) : AppConfig {
    override val isDebug: Boolean = BuildConfig.DEBUG
    override val applicationId: String = BuildConfig.APPLICATION_ID
    override val flavor: BuildFlavor = BuildFlavor.from(BuildConfig.FLAVOR)
    override val versionCode: Int = BuildConfig.VERSION_CODE
    override val versionName: String = BuildConfig.VERSION_NAME
    override val host: String = environmentConfiguration.apiHost
    override val humanVerificationHost: String = environmentConfiguration.hv3Host
    override val proxyToken: String? = environmentConfiguration.proxyToken.takeIfNotBlank()
    override val useDefaultPins: Boolean = environmentConfiguration.useDefaultPins
    override val sentryDSN: String? = BuildConfig.SENTRY_DSN.takeIf { !BuildConfig.DEBUG }
    override val accountSentryDSN: String? =
        BuildConfig.ACCOUNT_SENTRY_DSN.takeIf { !BuildConfig.DEBUG }
    override val androidVersion: Int = Build.VERSION.SDK_INT
}
