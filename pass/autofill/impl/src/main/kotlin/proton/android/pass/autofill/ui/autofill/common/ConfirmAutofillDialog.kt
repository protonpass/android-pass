/*
 * Copyright (c) 2023 Proton AG
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
    onConfirm: () -> Unit,
    onClose: () -> Unit
) {
    val (title, body) = when (mode) {
        AutofillConfirmMode.DangerousAutofill -> {
            R.string.autofill_confirm_dangerous_title to R.string.autofill_confirm_dangerous_body
        }
    }

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

            DialogCancelConfirmSection(
                color = PassTheme.colors.interactionNormMajor2,
                confirmText = stringResource(R.string.autofill_confirm_button),
                cancelText = stringResource(CompR.string.bottomsheet_cancel_button),
                onDismiss = onClose,
                onConfirm = onConfirm
            )
        }
    }
}
