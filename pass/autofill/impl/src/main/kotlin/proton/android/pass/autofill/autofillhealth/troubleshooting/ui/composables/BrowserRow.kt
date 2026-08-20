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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserAutofillCoverage
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserInfo
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import me.proton.core.presentation.R as CoreR

@Composable
internal fun BrowserRow(browser: BrowserInfo, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(vertical = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Text.Body1Regular(modifier = Modifier.weight(1f), text = browser.label)
        if (browser.canOpenAutofillSettings) {
            BrowserActiveTag()
        }
    }
}

@Composable
internal fun InactiveBrowserRow(
    browser: BrowserInfo,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable(onClick = onOpenClick)
            .padding(vertical = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Text.Body1Regular(modifier = Modifier.weight(1f), text = browser.label)
        Icon.Default(
            id = CoreR.drawable.ic_proton_chevron_right,
            contentDescription = null,
            tint = PassTheme.colors.textWeak
        )
    }
}

@Preview
@Composable
internal fun BrowserRowNameOnlyPreview() {
    PassTheme {
        Surface {
            BrowserRow(
                browser = BrowserInfo(
                    packageName = "org.mozilla.firefox",
                    label = "Firefox",
                    coverage = BrowserAutofillCoverage.NeedsSetup
                )
            )
        }
    }
}

@Preview
@Composable
internal fun BrowserRowActivePreview() {
    PassTheme {
        Surface {
            BrowserRow(
                browser = BrowserInfo(
                    packageName = "com.android.chrome",
                    label = "Chrome",
                    coverage = BrowserAutofillCoverage.Working,
                    canOpenAutofillSettings = true
                )
            )
        }
    }
}

@Preview
@Composable
internal fun InactiveBrowserRowPreview() {
    PassTheme {
        Surface {
            InactiveBrowserRow(
                browser = BrowserInfo(
                    packageName = "com.android.chrome",
                    label = "Chrome",
                    coverage = BrowserAutofillCoverage.NeedsSetup,
                    canOpenAutofillSettings = true
                ),
                onOpenClick = {}
            )
        }
    }
}
