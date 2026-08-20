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

package proton.android.pass.autofill.autofillhealth.troubleshooting.ui.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserAutofillCoverage
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserInfo
import proton.android.pass.autofill.service.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.text.Text
import me.proton.core.presentation.R as CoreR

@Composable
internal fun BrowsersSection(
    browsers: List<BrowserInfo>,
    onOpenBrowser: (BrowserInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val (inactiveBrowsers, compatibleBrowsers) = browsers.partition { browser ->
        browser.needsAutofillSetup
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        if (inactiveBrowsers.isNotEmpty()) {
            BrowsersGroup(
                iconRes = CoreR.drawable.ic_proton_exclamation_circle_filled,
                tint = PassTheme.colors.signalWarning,
                titleResId = R.string.autofill_troubleshooting_browsers_inactive_header,
                bodyResId = R.string.autofill_troubleshooting_browsers_inactive_body,
                browsers = inactiveBrowsers
            ) { browser ->
                InactiveBrowserRow(
                    browser = browser,
                    onOpenClick = { onOpenBrowser(browser) }
                )
            }
        }

        if (compatibleBrowsers.isNotEmpty()) {
            BrowsersGroup(
                iconRes = CoreR.drawable.ic_proton_checkmark_circle,
                tint = PassTheme.colors.signalSuccess,
                titleResId = R.string.autofill_troubleshooting_browsers_compatible_header,
                bodyResId = R.string.autofill_troubleshooting_browsers_compatible_body,
                browsers = compatibleBrowsers
            ) { browser ->
                BrowserRow(browser = browser)
            }
        }
    }
}

@Composable
private fun BrowsersGroup(
    @DrawableRes iconRes: Int,
    tint: Color,
    @StringRes titleResId: Int,
    @StringRes bodyResId: Int,
    browsers: List<BrowserInfo>,
    modifier: Modifier = Modifier,
    row: @Composable (BrowserInfo) -> Unit
) {
    TroubleshootingCard(modifier = modifier) {
        AutofillStatusRow(
            iconRes = iconRes,
            tint = tint,
            title = stringResource(titleResId)
        )
        Text.Body3Weak(stringResource(bodyResId))
        Column(modifier = Modifier.fillMaxWidth()) {
            browsers.forEachIndexed { index, browser ->
                if (index > 0) PassDivider()
                row(browser)
            }
        }
    }
}

@Preview
@Composable
internal fun BrowsersSectionPreview() {
    PassTheme {
        Surface {
            BrowsersSection(
                browsers = listOf(
                    BrowserInfo(
                        packageName = "com.android.chrome",
                        label = "Chrome",
                        coverage = BrowserAutofillCoverage.NeedsSetup,
                        canOpenAutofillSettings = true
                    ),
                    BrowserInfo(
                        packageName = "com.brave.browser",
                        label = "Brave",
                        coverage = BrowserAutofillCoverage.Working,
                        canOpenAutofillSettings = true
                    ),
                    BrowserInfo(
                        packageName = "com.microsoft.emmx",
                        label = "Edge",
                        coverage = BrowserAutofillCoverage.NeedsSetup
                    ),
                    BrowserInfo(
                        packageName = "org.mozilla.firefox",
                        label = "Firefox",
                        coverage = BrowserAutofillCoverage.NeedsSetup
                    )
                ),
                onOpenBrowser = {}
            )
        }
    }
}
