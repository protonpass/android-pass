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

package proton.android.pass.telemetry.impl.startup

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.telemetry.api.GooglePlayInstallReferrer
import proton.android.pass.telemetry.api.MetaInstallReferrer
import proton.android.pass.telemetry.api.TelemetryGrowthInstallEvent

class InstallReferrerMappingTest {

    @Test
    fun `organic install produces no referrer dimensions`() {
        val event = GooglePlayInstallReferrer().toInstallEvent(
            metaReferrer = MetaInstallReferrer(),
            currentTimeMs = CURRENT_TIME_MS
        )

        assertThat(event.dimensions()).doesNotContainKey(TelemetryGrowthInstallEvent.KEY_GOOGLE_CURRENT_DEVICE_TIME_MS)
        assertThat(event.dimensions().keys).containsExactly(TelemetryGrowthInstallEvent.KEY_IS_REINSTALL)
    }

    @Test
    fun `failed referrer lookup produces no referrer dimensions`() {
        val event = GooglePlayInstallReferrer().toInstallEvent(
            metaReferrer = MetaInstallReferrer(isClickThrough = null),
            currentTimeMs = CURRENT_TIME_MS
        )

        assertThat(event.dimensions().keys).containsExactly(TelemetryGrowthInstallEvent.KEY_IS_REINSTALL)
    }

    @Test
    fun `google referrer data populates device time and timestamps`() {
        val event = GooglePlayInstallReferrer(
            referrer = "utm_source=test",
            clickTimestampSeconds = 1_550_420_123L
        ).toInstallEvent(
            metaReferrer = MetaInstallReferrer(),
            currentTimeMs = CURRENT_TIME_MS
        )

        val dimensions = event.dimensions()
        assertThat(dimensions[TelemetryGrowthInstallEvent.KEY_INSTALL_REF]).isEqualTo("utm_source=test")
        assertThat(dimensions[TelemetryGrowthInstallEvent.KEY_GOOGLE_CLICK_TIMESTAMP_SECONDS])
            .isEqualTo("1550420123")
        assertThat(dimensions[TelemetryGrowthInstallEvent.KEY_GOOGLE_CURRENT_DEVICE_TIME_MS])
            .isEqualTo(CURRENT_TIME_MS.toString())
    }

    @Test
    fun `meta-only referrer data populates meta dimensions without google device time`() {
        val event = GooglePlayInstallReferrer().toInstallEvent(
            metaReferrer = MetaInstallReferrer(
                encryptedData = "encrypted-payload",
                attributionTimestampSeconds = 1_550_420_000L,
                isClickThrough = true
            ),
            currentTimeMs = CURRENT_TIME_MS
        )

        val dimensions = event.dimensions()
        assertThat(dimensions[TelemetryGrowthInstallEvent.KEY_META_INSTALL_REFERRER])
            .isEqualTo("encrypted-payload")
        assertThat(dimensions[TelemetryGrowthInstallEvent.KEY_META_ACTUAL_TIMESTAMP]).isEqualTo("1550420000")
        assertThat(dimensions[TelemetryGrowthInstallEvent.KEY_META_IS_CT]).isEqualTo("1")
        assertThat(dimensions).doesNotContainKey(TelemetryGrowthInstallEvent.KEY_GOOGLE_CURRENT_DEVICE_TIME_MS)
    }

    @Test
    fun `when both networks report a meta click through only meta parameters are sent`() {
        val event = GooglePlayInstallReferrer(
            referrer = "utm_source=apps.facebook.com&utm_content=payload",
            clickTimestampSeconds = 1_550_420_500L,
            installBeginTimestampSeconds = 1_550_420_600L
        ).toInstallEvent(
            metaReferrer = MetaInstallReferrer(
                encryptedData = "encrypted-payload",
                attributionTimestampSeconds = 1_550_420_000L,
                isClickThrough = true
            ),
            currentTimeMs = CURRENT_TIME_MS
        )

        val dimensions = event.dimensions()
        assertThat(dimensions[TelemetryGrowthInstallEvent.KEY_INSTALL_REF])
            .isEqualTo("utm_source=apps.facebook.com&utm_content=payload")
        assertThat(dimensions[TelemetryGrowthInstallEvent.KEY_META_INSTALL_REFERRER])
            .isEqualTo("encrypted-payload")
        assertThat(dimensions[TelemetryGrowthInstallEvent.KEY_META_IS_CT]).isEqualTo("1")
        assertThat(dimensions).doesNotContainKey(TelemetryGrowthInstallEvent.KEY_GOOGLE_CLICK_TIMESTAMP_SECONDS)
        assertThat(dimensions).doesNotContainKey(TelemetryGrowthInstallEvent.KEY_GOOGLE_INSTALL_BEGIN_SECONDS)
        assertThat(dimensions).doesNotContainKey(TelemetryGrowthInstallEvent.KEY_GOOGLE_CURRENT_DEVICE_TIME_MS)
    }

    @Test
    fun `when meta view through overlaps a google click only google parameters are sent`() {
        val event = GooglePlayInstallReferrer(
            referrer = "utm_source=googleads&gclid=abc",
            clickTimestampSeconds = 1_550_420_500L,
            installBeginTimestampSeconds = 1_550_420_600L
        ).toInstallEvent(
            metaReferrer = MetaInstallReferrer(
                encryptedData = "encrypted-payload",
                attributionTimestampSeconds = 1_550_420_000L,
                isClickThrough = false
            ),
            currentTimeMs = CURRENT_TIME_MS
        )

        val dimensions = event.dimensions()
        assertThat(dimensions[TelemetryGrowthInstallEvent.KEY_GOOGLE_CLICK_TIMESTAMP_SECONDS])
            .isEqualTo("1550420500")
        assertThat(dimensions[TelemetryGrowthInstallEvent.KEY_GOOGLE_INSTALL_BEGIN_SECONDS])
            .isEqualTo("1550420600")
        assertThat(dimensions[TelemetryGrowthInstallEvent.KEY_GOOGLE_CURRENT_DEVICE_TIME_MS])
            .isEqualTo(CURRENT_TIME_MS.toString())
        assertThat(dimensions).doesNotContainKey(TelemetryGrowthInstallEvent.KEY_META_INSTALL_REFERRER)
        assertThat(dimensions).doesNotContainKey(TelemetryGrowthInstallEvent.KEY_META_ACTUAL_TIMESTAMP)
        assertThat(dimensions).doesNotContainKey(TelemetryGrowthInstallEvent.KEY_META_IS_CT)
    }

    @Test
    fun `organic play referrer loses against meta view through data`() {
        val event = GooglePlayInstallReferrer(
            referrer = "utm_source=google-play&utm_medium=organic",
            clickTimestampSeconds = 1_550_420_500L,
            installBeginTimestampSeconds = 1_550_420_600L
        ).toInstallEvent(
            metaReferrer = MetaInstallReferrer(
                encryptedData = "encrypted-payload",
                attributionTimestampSeconds = 1_550_420_000L,
                isClickThrough = false
            ),
            currentTimeMs = CURRENT_TIME_MS
        )

        val dimensions = event.dimensions()
        assertThat(dimensions[TelemetryGrowthInstallEvent.KEY_META_INSTALL_REFERRER])
            .isEqualTo("encrypted-payload")
        assertThat(dimensions[TelemetryGrowthInstallEvent.KEY_META_ACTUAL_TIMESTAMP]).isEqualTo("1550420000")
        assertThat(dimensions[TelemetryGrowthInstallEvent.KEY_META_IS_CT]).isEqualTo("0")
        assertThat(dimensions).doesNotContainKey(TelemetryGrowthInstallEvent.KEY_GOOGLE_CLICK_TIMESTAMP_SECONDS)
        assertThat(dimensions).doesNotContainKey(TelemetryGrowthInstallEvent.KEY_GOOGLE_INSTALL_BEGIN_SECONDS)
        assertThat(dimensions).doesNotContainKey(TelemetryGrowthInstallEvent.KEY_GOOGLE_CURRENT_DEVICE_TIME_MS)
    }

    private companion object {
        const val CURRENT_TIME_MS = 1_700_000_000_000L
    }
}
