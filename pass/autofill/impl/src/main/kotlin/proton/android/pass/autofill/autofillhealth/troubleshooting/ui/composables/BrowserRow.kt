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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserAutofillCoverage
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserAutofillInstructions
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserInfo
import proton.android.pass.autofill.service.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import me.proton.core.presentation.R as CoreR

@Composable
internal fun BrowserRow(
    browser: BrowserInfo,
    isServiceEnabled: Boolean,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "BrowserRowChevronRotation"
    )
    Column(
        modifier = modifier
            .clickable { expanded = !expanded }
            .padding(Spacing.medium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            val (statusIconRes, statusTint) = when {
                !isServiceEnabled ->
                    CoreR.drawable.ic_proton_info_circle to PassTheme.colors.textWeak

                browser.coverage == BrowserAutofillCoverage.NeedsSetup ->
                    CoreR.drawable.ic_proton_exclamation_circle_filled to PassTheme.colors.signalWarning

                else ->
                    CoreR.drawable.ic_proton_checkmark_circle to PassTheme.colors.signalSuccess
            }
            Icon.Default(id = statusIconRes, contentDescription = null, tint = statusTint)
            Text.Body1Regular(modifier = Modifier.weight(1f), text = browser.label)
            Icon.Default(
                modifier = Modifier.rotate(chevronRotation),
                id = CoreR.drawable.ic_proton_chevron_down,
                contentDescription = null,
                tint = PassTheme.colors.textWeak
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(top = Spacing.small),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                when {
                    !isServiceEnabled -> Text.Body3Weak(
                        stringResource(R.string.autofill_troubleshooting_browser_pending_service)
                    )

                    browser.coverage == BrowserAutofillCoverage.Working -> Text.Body3Weak(
                        stringResource(R.string.autofill_troubleshooting_browser_working)
                    )

                    browser.coverage == BrowserAutofillCoverage.Ready -> Text.Body3Weak(
                        stringResource(R.string.autofill_troubleshooting_browser_ready)
                    )

                    else -> {
                        Text.Body3Weak(
                            stringResource(
                                BrowserAutofillInstructions.stepsResIdFor(browser.packageName)
                            )
                        )
                        Text.Body3Medium(
                            modifier = Modifier.clickable(onClick = onOpenClick),
                            text = stringResource(R.string.autofill_troubleshooting_browser_open_action),
                            color = PassTheme.colors.interactionNormMajor2
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
internal fun BrowserRowReadyPreview() {
    PassTheme {
        Surface {
            BrowserRow(
                browser = BrowserInfo(
                    packageName = "com.android.chrome",
                    label = "Chrome",
                    coverage = BrowserAutofillCoverage.Ready
                ),
                isServiceEnabled = true,
                onOpenClick = {}
            )
        }
    }
}

@Preview
@Composable
internal fun BrowserRowNeedsSetupPreview() {
    PassTheme {
        Surface {
            BrowserRow(
                browser = BrowserInfo(
                    packageName = "org.mozilla.firefox",
                    label = "Firefox",
                    coverage = BrowserAutofillCoverage.NeedsSetup
                ),
                isServiceEnabled = true,
                onOpenClick = {}
            )
        }
    }
}

@Preview
@Composable
internal fun BrowserRowWorkingPreview() {
    PassTheme {
        Surface {
            BrowserRow(
                browser = BrowserInfo(
                    packageName = "com.android.chrome",
                    label = "Chrome",
                    coverage = BrowserAutofillCoverage.Working
                ),
                isServiceEnabled = true,
                onOpenClick = {}
            )
        }
    }
}

@Preview
@Composable
internal fun BrowserRowPendingServicePreview() {
    PassTheme {
        Surface {
            BrowserRow(
                browser = BrowserInfo(
                    packageName = "com.android.chrome",
                    label = "Chrome",
                    coverage = BrowserAutofillCoverage.Ready
                ),
                isServiceEnabled = false,
                onOpenClick = {}
            )
        }
    }
}
