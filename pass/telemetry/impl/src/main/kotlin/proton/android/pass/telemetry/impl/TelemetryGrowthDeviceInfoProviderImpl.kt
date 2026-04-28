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

package proton.android.pass.telemetry.impl

import android.os.Build
import kotlinx.coroutines.flow.first
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.telemetry.api.TelemetryGrowthDeviceInfoProvider
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryGrowthDeviceInfoProviderImpl @Inject constructor(
    private val appConfig: AppConfig,
    private val internalSettingsRepository: InternalSettingsRepository
) : TelemetryGrowthDeviceInfoProvider {
    override val osVersion: String get() = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    override val appVersion: String get() = appConfig.versionName
    override val platform: String = "android"
    override val make: String get() = Build.MANUFACTURER
    override val model: String get() = Build.MODEL
    override val appPackageName: String get() = appConfig.applicationId
    override val locale: String get() = Locale.getDefault().toString()
    override val languageCode: String get() = Locale.getDefault().language
    override val appIdentifier: String get() = appConfig.applicationId

    override suspend fun getAsid(): String = internalSettingsRepository.getPersistentUUID().first().toString()
}
