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
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillHealthVerdict
import proton.android.pass.autofill.service.R
import proton.android.pass.commonui.api.PassTheme
import me.proton.core.presentation.R as CoreR

@Composable
internal fun HealthVerdictRow(verdict: AutofillHealthVerdict, modifier: Modifier = Modifier) {
    when (verdict) {
        AutofillHealthVerdict.Loading,
        AutofillHealthVerdict.Unsupported -> Unit

        AutofillHealthVerdict.AllGood -> AutofillStatusRow(
            modifier = modifier,
            iconRes = CoreR.drawable.ic_proton_checkmark_circle,
            tint = PassTheme.colors.signalSuccess,
            title = stringResource(R.string.autofill_troubleshooting_verdict_all_good)
        )

        AutofillHealthVerdict.NeedsAttention -> AutofillStatusRow(
            modifier = modifier,
            iconRes = CoreR.drawable.ic_proton_exclamation_circle_filled,
            tint = PassTheme.colors.signalWarning,
            title = stringResource(R.string.autofill_troubleshooting_verdict_attention)
        )
    }
}

@Preview
@Composable
internal fun HealthVerdictRowAllGoodPreview() {
    PassTheme {
        Surface {
            TroubleshootingCard {
                HealthVerdictRow(verdict = AutofillHealthVerdict.AllGood)
            }
        }
    }
}

@Preview
@Composable
internal fun HealthVerdictNeedsAttnPreview() {
    PassTheme {
        Surface {
            TroubleshootingCard {
                HealthVerdictRow(verdict = AutofillHealthVerdict.NeedsAttention)
            }
        }
    }
}
