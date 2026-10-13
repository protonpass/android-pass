/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton Pass.
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

package proton.android.pass.data.impl.util

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test
import proton.android.pass.data.impl.requests.TelemetryGrowthInstallEventRequest
import proton.android.pass.telemetry.api.TelemetryGrowthEventNames
import proton.android.pass.telemetry.api.TelemetryGrowthInstallEvent
import proton.android.pass.telemetry.fakes.FakeTelemetryGrowthDeviceInfoProvider

class TelemetryGrowthRequestBuilderTest {

    @Test
    fun `builds install request without referrer parameters when dimensions are absent`() {
        val request = buildRequest(
            mapOf(
                TelemetryGrowthInstallEvent.KEY_IS_REINSTALL to JsonPrimitive("false"),
                TelemetryGrowthInstallEvent.KEY_INSTALL_REF to JsonPrimitive("utm_source=test")
            )
        )

        assertThat(request).isInstanceOf(TelemetryGrowthInstallEventRequest::class.java)
        val installRequest = request as TelemetryGrowthInstallEventRequest
        assertThat(installRequest.installRef).isEqualTo("utm_source=test")
        assertThat(installRequest.installRefParameters).isNull()
    }

    @Test
    fun `builds install request with google and meta referrer parameters`() {
        val referrer = """{"utm_source":"googleads","gclid":"abc"}"""
        val request = buildRequest(
            mapOf(
                TelemetryGrowthInstallEvent.KEY_IS_REINSTALL to JsonPrimitive("false"),
                TelemetryGrowthInstallEvent.KEY_INSTALL_REF to JsonPrimitive(referrer),
                TelemetryGrowthInstallEvent.KEY_GOOGLE_CLICK_TIMESTAMP_SECONDS to JsonPrimitive("1550420123"),
                TelemetryGrowthInstallEvent.KEY_GOOGLE_INSTALL_BEGIN_SECONDS to JsonPrimitive("1550420200"),
                TelemetryGrowthInstallEvent.KEY_GOOGLE_CURRENT_DEVICE_TIME_MS to JsonPrimitive("1550420454906"),
                TelemetryGrowthInstallEvent.KEY_META_INSTALL_REFERRER to JsonPrimitive("encrypted-payload"),
                TelemetryGrowthInstallEvent.KEY_META_ACTUAL_TIMESTAMP to JsonPrimitive("1550420000"),
                TelemetryGrowthInstallEvent.KEY_META_IS_CT to JsonPrimitive("1")
            )
        )

        val installRequest = request as TelemetryGrowthInstallEventRequest
        val parameters = installRequest.installRefParameters
        assertThat(parameters).isNotNull()
        assertThat(parameters!!.goClickTimestampSeconds).isEqualTo(1_550_420_123L)
        assertThat(parameters.goInstallBeginTimestampSeconds).isEqualTo(1_550_420_200L)
        assertThat(parameters.goCurrentDeviceTime).isEqualTo(1_550_420_454_906L)
        assertThat(parameters.meInstallReferrer).isEqualTo("encrypted-payload")
        assertThat(parameters.meActualTimestamp).isEqualTo(1_550_420_000L)
        assertThat(parameters.meIsCt).isEqualTo(1)
    }

    @Test
    fun `ignores malformed referrer parameter values`() {
        val request = buildRequest(
            mapOf(
                TelemetryGrowthInstallEvent.KEY_IS_REINSTALL to JsonPrimitive("false"),
                TelemetryGrowthInstallEvent.KEY_GOOGLE_CLICK_TIMESTAMP_SECONDS to JsonPrimitive("not-a-number"),
                TelemetryGrowthInstallEvent.KEY_META_IS_CT to JsonPrimitive("maybe")
            )
        )

        val installRequest = request as TelemetryGrowthInstallEventRequest
        assertThat(installRequest.installRefParameters).isNull()
    }

    private fun buildRequest(dimensions: Map<String, JsonPrimitive>) = buildTelemetryGrowthEventRequest(
        eventType = TelemetryGrowthEventNames.Install,
        timestampMs = 1_000L,
        dimensions = dimensions,
        asid = "fake-asid",
        info = FakeTelemetryGrowthDeviceInfoProvider()
    )
}
