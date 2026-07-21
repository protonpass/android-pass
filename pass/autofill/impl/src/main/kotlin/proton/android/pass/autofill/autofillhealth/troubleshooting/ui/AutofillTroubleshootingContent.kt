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

package proton.android.pass.autofill.autofillhealth.troubleshooting.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserAutofillCoverage
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.BrowserInfo
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillHealthVerdict
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillHealthVerdictResolver
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillServiceStatus
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillTroubleshootingState
import proton.android.pass.autofill.autofillhealth.troubleshooting.ui.composables.AutofillServiceSection
import proton.android.pass.autofill.autofillhealth.troubleshooting.ui.composables.BrowsersSection
import proton.android.pass.autofill.autofillhealth.troubleshooting.ui.composables.HealthVerdictRow
import proton.android.pass.autofill.autofillhealth.troubleshooting.ui.composables.InlineSuggestionsRow
import proton.android.pass.autofill.autofillhealth.troubleshooting.ui.composables.TroubleshootingCard
import proton.android.pass.autofill.service.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar

@Composable
internal fun AutofillTroubleshootingContent(
    state: AutofillTroubleshootingState,
    onEvent: (AutofillTroubleshootingUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            PassExtendedTopBar(
                title = stringResource(R.string.autofill_troubleshooting_title),
                onUpClick = { onEvent(AutofillTroubleshootingUiEvent.OnBack) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.medium)
                    .padding(bottom = Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                Text.Body3Weak(stringResource(R.string.autofill_troubleshooting_subtitle))

                val verdict = AutofillHealthVerdictResolver.resolve(state)
                val showVerdict = verdict == AutofillHealthVerdict.AllGood ||
                    verdict == AutofillHealthVerdict.NeedsAttention
                val showService = state.serviceStatus != AutofillServiceStatus.Loading
                val showBrowsers = state.installedBrowsers.isNotEmpty()

                TroubleshootingCard {
                    if (showVerdict) {
                        HealthVerdictRow(verdict = verdict)
                    }

                    if (showService) {
                        if (showVerdict) PassDivider()
                        AutofillServiceSection(
                            status = state.serviceStatus,
                            onEnableClick = { onEvent(AutofillTroubleshootingUiEvent.OnEnableAutofill) },
                            conflictingServiceLabel = state.conflictingServiceLabel
                        )
                    }

                    if (showBrowsers) {
                        if (showVerdict || showService) PassDivider()
                        BrowsersSection(
                            browsers = state.installedBrowsers,
                            isServiceEnabled =
                            state.serviceStatus == AutofillServiceStatus.EnabledByOurService,
                            onOpenBrowser = { browser ->
                                onEvent(AutofillTroubleshootingUiEvent.OnOpenBrowser(browser))
                            }
                        )
                    }

                    if (showVerdict || showService || showBrowsers) PassDivider()
                    InlineSuggestionsRow()
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = Spacing.medium)
                    .padding(top = Spacing.medium, bottom = Spacing.large),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                PassCircleButton(
                    text = stringResource(R.string.autofill_troubleshooting_share_diagnostics),
                    backgroundColor = PassTheme.colors.interactionNormMinor1,
                    textColor = PassTheme.colors.interactionNormMajor2,
                    onClick = { onEvent(AutofillTroubleshootingUiEvent.OnShareDiagnostics) }
                )

                PassCircleButton(
                    text = stringResource(R.string.autofill_troubleshooting_open_diagnostics),
                    backgroundColor = PassTheme.colors.backgroundStrong,
                    textColor = PassTheme.colors.textNorm,
                    onClick = { onEvent(AutofillTroubleshootingUiEvent.OnOpenDiagnostics) }
                )
            }
        }
    }
}

internal class AutofillTroubleshootingPreviewProvider :
    PreviewParameterProvider<AutofillTroubleshootingState> {
    override val values: Sequence<AutofillTroubleshootingState> = sequenceOf(
        AutofillTroubleshootingState(
            serviceStatus = AutofillServiceStatus.NotDefault,
            installedBrowsers = listOf(
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
            supportsInlineSuggestions = true,
            conflictingServiceLabel = "Google"
        ),
        AutofillTroubleshootingState(
            serviceStatus = AutofillServiceStatus.EnabledByOurService,
            installedBrowsers = listOf(
                BrowserInfo(
                    packageName = "com.android.chrome",
                    label = "Chrome",
                    coverage = BrowserAutofillCoverage.Ready
                )
            ),
            supportsInlineSuggestions = true
        )
    )
}

internal class ThemedAutofillTroubleshootingPreviewProvider :
    ThemePairPreviewProvider<AutofillTroubleshootingState>(AutofillTroubleshootingPreviewProvider())

@Preview
@Composable
internal fun AutofillTroubleshootingContentPreview(
    @PreviewParameter(ThemedAutofillTroubleshootingPreviewProvider::class)
    input: Pair<Boolean, AutofillTroubleshootingState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AutofillTroubleshootingContent(state = input.second, onEvent = {})
        }
    }
}
