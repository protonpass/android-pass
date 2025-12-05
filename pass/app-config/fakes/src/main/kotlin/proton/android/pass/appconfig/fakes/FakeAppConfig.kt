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

package proton.android.pass.appconfig.fakes

import android.os.Build
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildEnv
import proton.android.pass.appconfig.api.BuildFlavor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAppConfig @Inject constructor() : AppConfig {

    private var androidVersionValue = Build.VERSION_CODES.TIRAMISU

    override val isDebug: Boolean
        get() = false
    override val applicationId: String
        get() = ""
    override val flavor: BuildFlavor
        get() = BuildFlavor.Play(BuildEnv.PROD)
    override val versionCode: Int
        get() = 0
    override val versionName: String
        get() = "0.0.0"
    override val host: String
        get() = ""
    override val humanVerificationHost: String
        get() = ""
    override val proxyToken: String
        get() = ""
    override val useDefaultPins: Boolean
        get() = true
    override val sentryDSN: String
        get() = ""
    override val accountSentryDSN: String
        get() = ""
    override val androidVersion: Int
        get() = androidVersionValue
    override val allowScreenshotsDefaultValue: Boolean
        get() = true

    fun setAndroidVersion(value: Int) {
        androidVersionValue = value
    }
}
