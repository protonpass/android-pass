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

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillAttempt
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillAttemptOutcome
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillServiceStatus
import proton.android.pass.autofill.service.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.text.Text
import me.proton.core.presentation.R as CoreR

@Composable
internal fun AutofillLastAttemptCard(
    serviceStatus: AutofillServiceStatus,
    lastAttempt: AutofillAttempt?,
    modifier: Modifier = Modifier
) {
    TroubleshootingCard(modifier) {
        Text.Body1Medium(stringResource(R.string.autofill_diagnostics_last_attempt_title))

        when {
            serviceStatus != AutofillServiceStatus.EnabledByOurService -> AutofillStatusRow(
                iconRes = CoreR.drawable.ic_proton_exclamation_circle_filled,
                tint = PassTheme.colors.signalWarning,
                title = stringResource(R.string.autofill_diagnostics_reason_not_default)
            )

            lastAttempt == null -> AutofillStatusRow(
                iconRes = CoreR.drawable.ic_proton_info_circle,
                tint = PassTheme.colors.textWeak,
                title = stringResource(R.string.autofill_diagnostics_no_attempts)
            )

            else -> when (lastAttempt.outcome) {
                AutofillAttemptOutcome.Offered -> AutofillStatusRow(
                    iconRes = CoreR.drawable.ic_proton_checkmark_circle,
                    tint = PassTheme.colors.signalSuccess,
                    title = stringResource(
                        R.string.autofill_diagnostics_outcome_offered,
                        lastAttempt.displayTarget
                    )
                )

                AutofillAttemptOutcome.NothingOffered -> AutofillStatusRow(
                    iconRes = CoreR.drawable.ic_proton_exclamation_circle_filled,
                    tint = PassTheme.colors.signalWarning,
                    title = stringResource(
                        R.string.autofill_diagnostics_outcome_nothing,
                        lastAttempt.displayTarget
                    )
                )

                AutofillAttemptOutcome.Error -> AutofillStatusRow(
                    iconRes = CoreR.drawable.ic_proton_exclamation_circle_filled,
                    tint = PassTheme.colors.signalDanger,
                    title = stringResource(
                        R.string.autofill_diagnostics_outcome_error,
                        lastAttempt.displayTarget
                    )
                )
            }
        }
    }
}

@Preview
@Composable
internal fun AutofillLastAttemptCardPreview() {
    PassTheme {
        Surface {
            AutofillLastAttemptCard(
                serviceStatus = AutofillServiceStatus.EnabledByOurService,
                lastAttempt = AutofillAttempt(
                    packageName = "com.android.chrome",
                    appLabel = "Chrome",
                    webDomain = "example.com",
                    outcome = AutofillAttemptOutcome.NothingOffered,
                    timestamp = 0L
                )
            )
        }
    }
}
