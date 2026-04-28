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

package proton.android.pass.telemetry.fakes

import proton.android.pass.telemetry.api.TelemetryGrowthDeviceInfoProvider
import javax.inject.Inject

class FakeTelemetryGrowthDeviceInfoProvider @Inject constructor() : TelemetryGrowthDeviceInfoProvider {
    override val osVersion: String = "Android 13 (API 33)"
    override val appVersion: String = "1.0.0"
    override val platform: String = "android"
    override val make: String = "FakeMake"
    override val model: String = "FakeModel"
    override val appPackageName: String = "test.app.id"
    override val locale: String = "en_US"
    override val languageCode: String = "en"
    override val appIdentifier: String = "test.app.id"

    override suspend fun getAsid(): String = "fake-asid"
}
