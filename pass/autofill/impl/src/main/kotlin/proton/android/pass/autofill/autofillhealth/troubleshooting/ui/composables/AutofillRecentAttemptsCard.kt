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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillAttempt
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillAttemptOutcome
import proton.android.pass.autofill.service.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import me.proton.core.presentation.R as CoreR

@Composable
internal fun AutofillRecentAttemptsCard(attempts: List<AutofillAttempt>, modifier: Modifier = Modifier) {
    TroubleshootingCard(modifier) {
        Text.Body1Medium(stringResource(R.string.autofill_diagnostics_recent_title))
        attempts.forEach { attempt ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Icon.Default(
                    id = attempt.outcome.iconRes(),
                    contentDescription = null,
                    tint = attempt.outcome.tint()
                )
                Text.Body2Regular(text = attempt.displayTarget)
            }
        }
    }
}

@Composable
private fun AutofillAttemptOutcome.iconRes(): Int = when (this) {
    AutofillAttemptOutcome.Offered -> CoreR.drawable.ic_proton_checkmark_circle
    AutofillAttemptOutcome.NothingOffered -> CoreR.drawable.ic_proton_exclamation_circle_filled
    AutofillAttemptOutcome.Error -> CoreR.drawable.ic_proton_exclamation_circle_filled
}

@Composable
private fun AutofillAttemptOutcome.tint(): Color = when (this) {
    AutofillAttemptOutcome.Offered -> PassTheme.colors.signalSuccess
    AutofillAttemptOutcome.NothingOffered -> PassTheme.colors.signalWarning
    AutofillAttemptOutcome.Error -> PassTheme.colors.signalDanger
}

@Preview
@Composable
internal fun AutofillRecentAttemptsCardPreview() {
    PassTheme {
        Surface {
            AutofillRecentAttemptsCard(
                attempts = listOf(
                    AutofillAttempt(
                        "com.android.chrome",
                        "Chrome",
                        "example.com",
                        AutofillAttemptOutcome.NothingOffered,
                        0L
                    ),
                    AutofillAttempt(
                        "org.mozilla.firefox",
                        "Firefox",
                        "proton.me",
                        AutofillAttemptOutcome.Offered,
                        0L
                    )
                )
            )
        }
    }
}
