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
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import proton.android.pass.autofill.autofillhealth.troubleshooting.presentation.AutofillServiceStatus
import proton.android.pass.autofill.service.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.composecomponents.impl.text.Text
import me.proton.core.presentation.R as CoreR

@Composable
internal fun AutofillServiceSection(
    status: AutofillServiceStatus,
    onEnableClick: () -> Unit,
    modifier: Modifier = Modifier,
    conflictingServiceLabel: String? = null
) {
    when (status) {
        AutofillServiceStatus.Loading -> Unit

        AutofillServiceStatus.EnabledByOurService -> AutofillStatusRow(
            modifier = modifier,
            iconRes = CoreR.drawable.ic_proton_checkmark_circle,
            tint = PassTheme.colors.signalSuccess,
            title = stringResource(R.string.autofill_troubleshooting_service_ok)
        )

        AutofillServiceStatus.NotDefault -> Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            AutofillStatusRow(
                iconRes = CoreR.drawable.ic_proton_exclamation_circle_filled,
                tint = PassTheme.colors.signalWarning,
                title = stringResource(R.string.autofill_troubleshooting_service_not_default_title)
            )
            if (conflictingServiceLabel != null) {
                Text.Body3Weak(
                    stringResource(
                        R.string.autofill_troubleshooting_service_current_other,
                        conflictingServiceLabel
                    )
                )
            }
            Text.Body3Weak(stringResource(R.string.autofill_troubleshooting_service_not_default_body))
            PassCircleButton(
                fillMaxWidth = false,
                contentHorizontalPadding = Spacing.large,
                text = stringResource(R.string.autofill_troubleshooting_service_enable_action),
                onClick = onEnableClick
            )
        }

        AutofillServiceStatus.Unsupported -> AutofillStatusRow(
            modifier = modifier,
            iconRes = CoreR.drawable.ic_proton_exclamation_circle_filled,
            tint = PassTheme.colors.signalDanger,
            title = stringResource(R.string.autofill_troubleshooting_service_unsupported_title)
        )
    }
}

@Preview
@Composable
internal fun SvcSectionNotDefaultPreview() {
    PassTheme {
        Surface {
            TroubleshootingCard {
                AutofillServiceSection(
                    status = AutofillServiceStatus.NotDefault,
                    onEnableClick = {}
                )
            }
        }
    }
}

@Preview
@Composable
internal fun SvcSectionConflictPreview() {
    PassTheme {
        Surface {
            TroubleshootingCard {
                AutofillServiceSection(
                    status = AutofillServiceStatus.NotDefault,
                    onEnableClick = {},
                    conflictingServiceLabel = "Google"
                )
            }
        }
    }
}

@Preview
@Composable
internal fun SvcSectionEnabledPreview() {
    PassTheme {
        Surface {
            TroubleshootingCard {
                AutofillServiceSection(
                    status = AutofillServiceStatus.EnabledByOurService,
                    onEnableClick = {}
                )
            }
        }
    }
}
