/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.autofill.ui.autofill.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonDialogTitle
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultUnspecified
import proton.android.pass.autofill.service.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.dialogs.DialogCancelConfirmSection
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun ConfirmAutofillDialog(
    modifier: Modifier = Modifier,
    mode: AutofillConfirmMode,
    onConfirm: (rememberChoice: Boolean) -> Unit,
    onClose: () -> Unit
) {
    val (title, body) = when (mode) {
        AutofillConfirmMode.DangerousAutofill ->
            R.string.autofill_confirm_dangerous_title to R.string.autofill_confirm_dangerous_body

        AutofillConfirmMode.UnverifiedBrowser ->
            R.string.autofill_confirm_unverified_browser_title to R.string.autofill_confirm_unverified_browser_body
    }
    val (rememberChoice, onRememberChoiceChanged) = remember { mutableStateOf(false) }

    NoPaddingDialog(
        modifier = modifier,
        onDismissRequest = onClose
    ) {
        Column(
            modifier = Modifier
                .consumeObscuredTouches()
                .padding(horizontal = 24.dp, vertical = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
        ) {
            ProtonDialogTitle(
                title = stringResource(title)
            )

            Text(
                text = stringResource(body),
                style = ProtonTheme.typography.defaultUnspecified
            )

            if (mode == AutofillConfirmMode.UnverifiedBrowser) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRememberChoiceChanged(!rememberChoice) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        colors = CheckboxDefaults.colors(
                            checkedColor = PassTheme.colors.interactionNormMajor1,
                            checkmarkColor = PassTheme.colors.textInvert
                        ),
                        checked = rememberChoice,
                        onCheckedChange = onRememberChoiceChanged
                    )
                    Text(
                        text = stringResource(R.string.autofill_confirm_dont_ask_again),
                        style = ProtonTheme.typography.defaultUnspecified
                    )
                }
            }

            DialogCancelConfirmSection(
                color = PassTheme.colors.interactionNormMajor2,
                confirmText = stringResource(R.string.autofill_confirm_button),
                cancelText = stringResource(CompR.string.bottomsheet_cancel_button),
                onDismiss = onClose,
                onConfirm = {
                    onConfirm(mode == AutofillConfirmMode.UnverifiedBrowser && rememberChoice)
                }
            )
        }
    }
}
