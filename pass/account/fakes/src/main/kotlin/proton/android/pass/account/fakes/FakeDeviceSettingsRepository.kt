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

package proton.android.pass.account.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.proton.core.usersettings.domain.entity.DeviceSettings
import me.proton.core.usersettings.domain.repository.DeviceSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeDeviceSettingsRepository @Inject constructor() : DeviceSettingsRepository {

    private val deviceSettingsFlow = MutableStateFlow(
        DeviceSettings(
            isTelemetryEnabled = true, isCrashReportEnabled = true
        )
    )

    override suspend fun getDeviceSettings(): DeviceSettings = deviceSettingsFlow.value

    override fun observeDeviceSettings(): Flow<DeviceSettings> = deviceSettingsFlow

    override suspend fun updateIsCrashReportEnabled(isEnabled: Boolean) {
        deviceSettingsFlow.update { it.copy(isCrashReportEnabled = isEnabled) }
    }

    override suspend fun updateIsTelemetryEnabled(isEnabled: Boolean) {
        deviceSettingsFlow.update { it.copy(isTelemetryEnabled = isEnabled) }
    }
}
