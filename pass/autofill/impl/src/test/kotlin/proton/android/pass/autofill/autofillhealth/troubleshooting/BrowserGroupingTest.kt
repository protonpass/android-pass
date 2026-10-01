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

package proton.android.pass.autofill.autofillhealth.troubleshooting

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.autofill.NATIVE_AUTOFILL_BROWSERS
import proton.android.pass.autofill.ThirdPartyMode
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserAutofillCoverage
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserAutofillCoverageResolver
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserInfo

class BrowserGroupingTest {

    @Test
    fun `chrome is inactive when third party mode is off`() {
        assertThat(chrome().needsAutofillSetup).isTrue()
    }

    @Test
    fun `brave is inactive when third party mode is off`() {
        assertThat(brave().needsAutofillSetup).isTrue()
    }

    @Test
    fun `vivaldi is inactive when third party mode is off`() {
        assertThat(vivaldi().needsAutofillSetup).isTrue()
    }

    @Test
    fun `edge is compatible because we cannot open its autofill settings`() {
        val edge = edge()
        assertThat(edge.coverage).isEqualTo(BrowserAutofillCoverage.NeedsSetup)
        assertThat(edge.canOpenAutofillSettings).isFalse()
        assertThat(edge.needsAutofillSetup).isFalse()
    }

    @Test
    fun `firefox is ready because it supports native autofill`() {
        val firefox = firefox()
        assertThat(firefox.coverage).isEqualTo(BrowserAutofillCoverage.Ready)
        assertThat(firefox.canOpenAutofillSettings).isFalse()
        assertThat(firefox.needsAutofillSetup).isFalse()
    }

    @Test
    fun `opera is compatible because compat mode covers it`() {
        val opera = opera()
        assertThat(opera.coverage).isEqualTo(BrowserAutofillCoverage.Ready)
        assertThat(opera.needsAutofillSetup).isFalse()
    }

    @Test
    fun `browsers split between the inactive and the compatible group`() {
        val browsers = listOf(chrome(), brave(), vivaldi(), edge(), firefox(), opera())

        val (inactive, compatible) = browsers.partition { it.needsAutofillSetup }

        assertThat(inactive.map { it.label })
            .containsExactly("Chrome", "Brave", "Vivaldi")
        assertThat(compatible.map { it.label })
            .containsExactly("Edge", "Firefox", "Opera")
    }

    @Test
    fun `edge stays compatible whether its toggle is on or off`() {
        assertThat(edge(ThirdPartyMode.Enabled).coverage)
            .isEqualTo(BrowserAutofillCoverage.Ready)
        assertThat(edge(ThirdPartyMode.Enabled).needsAutofillSetup).isFalse()
        assertThat(edge().needsAutofillSetup).isFalse()
    }

    @Test
    fun `a browser seen working stays compatible without being configurable`() {
        val firefox = firefox(hasBeenSeenWorking = true)

        assertThat(firefox.coverage).isEqualTo(BrowserAutofillCoverage.Working)
        assertThat(firefox.needsAutofillSetup).isFalse()
    }

    @Test
    fun `chrome stays inactive even after being seen working`() {
        val chrome = chrome(hasBeenSeenWorking = true)

        assertThat(chrome.coverage).isEqualTo(BrowserAutofillCoverage.NeedsSetup)
        assertThat(chrome.needsAutofillSetup).isTrue()
    }

    private fun chrome(hasBeenSeenWorking: Boolean = false) = browser(
        packageName = "com.android.chrome",
        label = "Chrome",
        versionCode = 787_118_633,
        compatMaxVersionCode = 711_900_039,
        thirdPartyMode = ThirdPartyMode.Disabled,
        canOpenAutofillSettings = true,
        hasBeenSeenWorking = hasBeenSeenWorking
    )

    private fun brave() = browser(
        packageName = "com.brave.browser",
        label = "Brave",
        versionCode = 427_912_623,
        compatMaxVersionCode = 427_912_623,
        thirdPartyMode = ThirdPartyMode.Disabled,
        canOpenAutofillSettings = true
    )

    private fun vivaldi() = browser(
        packageName = "com.vivaldi.browser",
        label = "Vivaldi",
        versionCode = 540_990_100,
        compatMaxVersionCode = SENTINEL_CAP,
        thirdPartyMode = ThirdPartyMode.Disabled,
        canOpenAutofillSettings = true
    )

    private fun edge(thirdPartyMode: ThirdPartyMode = ThirdPartyMode.Disabled) = browser(
        packageName = "com.microsoft.emmx",
        label = "Edge",
        versionCode = 412_907_023,
        compatMaxVersionCode = SENTINEL_CAP,
        thirdPartyMode = thirdPartyMode,
        canOpenAutofillSettings = false
    )

    private fun firefox(hasBeenSeenWorking: Boolean = false) = browser(
        packageName = "org.mozilla.firefox",
        label = "Firefox",
        versionCode = 2_016_178_695,
        compatMaxVersionCode = 2_015_836_711,
        thirdPartyMode = ThirdPartyMode.Unknown,
        canOpenAutofillSettings = false,
        hasBeenSeenWorking = hasBeenSeenWorking,
        supportsNativeAutofill = "org.mozilla.firefox" in NATIVE_AUTOFILL_BROWSERS
    )

    private fun opera() = browser(
        packageName = "com.opera.browser",
        label = "Opera",
        versionCode = 1_910_012_408,
        compatMaxVersionCode = SENTINEL_CAP,
        thirdPartyMode = ThirdPartyMode.Unknown,
        canOpenAutofillSettings = false
    )

    @Suppress("LongParameterList")
    private fun browser(
        packageName: String,
        label: String,
        versionCode: Long,
        compatMaxVersionCode: Long?,
        thirdPartyMode: ThirdPartyMode,
        canOpenAutofillSettings: Boolean,
        hasBeenSeenWorking: Boolean = false,
        supportsNativeAutofill: Boolean = false
    ) = BrowserInfo(
        packageName = packageName,
        label = label,
        coverage = BrowserAutofillCoverageResolver.resolve(
            browserVersionCode = versionCode,
            compatMaxVersionCode = compatMaxVersionCode,
            hasBeenSeenWorking = hasBeenSeenWorking,
            thirdPartyMode = thirdPartyMode,
            supportsNativeAutofill = supportsNativeAutofill
        ),
        canOpenAutofillSettings = canOpenAutofillSettings
    )

    private companion object {
        private const val SENTINEL_CAP = 10_000_000_000L
    }
}
