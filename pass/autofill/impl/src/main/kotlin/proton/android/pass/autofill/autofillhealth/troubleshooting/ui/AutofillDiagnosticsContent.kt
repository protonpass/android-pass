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
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillAttempt
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillAttemptOutcome
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillDiagnosticsState
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillServiceStatus
import proton.android.pass.autofill.autofillhealth.troubleshooting.ui.composables.AutofillLastAttemptCard
import proton.android.pass.autofill.autofillhealth.troubleshooting.ui.composables.AutofillRecentAttemptsCard
import proton.android.pass.autofill.service.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.composecomponents.impl.setting.SettingToggle
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar

@Composable
internal fun AutofillDiagnosticsContent(
    state: AutofillDiagnosticsState,
    onEvent: (AutofillDiagnosticsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            PassExtendedTopBar(
                title = stringResource(R.string.autofill_diagnostics_title),
                onUpClick = { onEvent(AutofillDiagnosticsUiEvent.OnBack) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = Spacing.large)
        ) {
            SettingToggle(
                text = stringResource(R.string.autofill_diagnostics_enable),
                isChecked = state.isDiagnosticsEnabled,
                belowContent = {
                    Text.Body3Weak(stringResource(R.string.autofill_diagnostics_enable_description))
                },
                onClick = { onEvent(AutofillDiagnosticsUiEvent.OnToggleDiagnostics(it)) }
            )

            if (state.isDiagnosticsEnabled) {
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    Text.Body3Weak(stringResource(R.string.autofill_diagnostics_instructions))

                    AutofillLastAttemptCard(
                        serviceStatus = state.serviceStatus,
                        lastAttempt = state.lastAttempt
                    )

                    if (state.recentAttempts.isNotEmpty()) {
                        AutofillRecentAttemptsCard(attempts = state.recentAttempts)

                        PassCircleButton(
                            text = stringResource(R.string.autofill_diagnostics_clear),
                            backgroundColor = PassTheme.colors.backgroundStrong,
                            textColor = PassTheme.colors.textNorm,
                            onClick = { onEvent(AutofillDiagnosticsUiEvent.OnClearAttempts) }
                        )
                    }

                    PassCircleButton(
                        text = stringResource(R.string.autofill_diagnostics_share),
                        backgroundColor = PassTheme.colors.interactionNormMinor1,
                        textColor = PassTheme.colors.interactionNormMajor2,
                        onClick = { onEvent(AutofillDiagnosticsUiEvent.OnShare) }
                    )
                }
            }
        }
    }
}

internal class AutofillDiagnosticsPreviewProvider :
    PreviewParameterProvider<AutofillDiagnosticsState> {
    override val values: Sequence<AutofillDiagnosticsState> = sequenceOf(
        AutofillDiagnosticsState(isDiagnosticsEnabled = false),
        AutofillDiagnosticsState(
            isDiagnosticsEnabled = true,
            serviceStatus = AutofillServiceStatus.EnabledByOurService,
            lastAttempt = AutofillAttempt(
                packageName = "com.android.chrome",
                appLabel = "Chrome",
                webDomain = "example.com",
                outcome = AutofillAttemptOutcome.NothingOffered,
                timestamp = 0L
            ),
            recentAttempts = listOf(
                AutofillAttempt(
                    packageName = "com.android.chrome",
                    appLabel = "Chrome",
                    webDomain = "example.com",
                    outcome = AutofillAttemptOutcome.NothingOffered,
                    timestamp = 0L
                ),
                AutofillAttempt(
                    packageName = "com.example.app",
                    appLabel = "Sample App",
                    webDomain = null,
                    outcome = AutofillAttemptOutcome.Offered,
                    timestamp = 0L
                )
            )
        )
    )
}

internal class ThemedAutofillDiagnosticsPreviewProvider :
    ThemePairPreviewProvider<AutofillDiagnosticsState>(AutofillDiagnosticsPreviewProvider())

@Preview
@Composable
internal fun AutofillDiagnosticsContentPreview(
    @PreviewParameter(ThemedAutofillDiagnosticsPreviewProvider::class)
    input: Pair<Boolean, AutofillDiagnosticsState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AutofillDiagnosticsContent(state = input.second, onEvent = {})
        }
    }
}
