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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserAutofillCoverage
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserInfo
import proton.android.pass.autofill.service.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.text.Text

@Composable
internal fun BrowsersSection(
    browsers: List<BrowserInfo>,
    onOpenBrowser: (BrowserInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Text.Body2Medium(stringResource(R.string.autofill_troubleshooting_browsers_header))
        Text.Body3Weak(stringResource(R.string.autofill_troubleshooting_browsers_body))
        Column(modifier = Modifier.fillMaxWidth().roundedContainerNorm()) {
            browsers.forEachIndexed { index, browser ->
                if (index > 0) PassDivider()
                BrowserRow(
                    browser = browser,
                    onOpenClick = { onOpenBrowser(browser) }
                )
            }
        }
    }
}

@Preview
@Composable
internal fun BrowsersSectionPreview() {
    PassTheme {
        Surface {
            TroubleshootingCard {
                BrowsersSection(
                    browsers = listOf(
                        BrowserInfo(
                            packageName = "org.mozilla.firefox",
                            label = "Firefox",
                            coverage = BrowserAutofillCoverage.NeedsSetup
                        ),
                        BrowserInfo(
                            packageName = "com.android.chrome",
                            label = "Chrome",
                            coverage = BrowserAutofillCoverage.Ready
                        )
                    ),
                    onOpenBrowser = {}
                )
            }
        }
    }
}
