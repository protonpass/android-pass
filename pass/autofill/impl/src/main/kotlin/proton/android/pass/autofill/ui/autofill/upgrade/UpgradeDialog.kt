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

package proton.android.pass.autofill.ui.autofill.upgrade

import androidx.compose.foundation.isSystemInDarkTheme
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
import proton.android.pass.composecomponents.impl.dialogs.DialogCancelConfirmSection
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun UpgradeDialog(
    modifier: Modifier = Modifier,
    onUpgrade: () -> Unit,
    onClose: () -> Unit
) {
    PassTheme(isDark = isSystemInDarkTheme()) {
        NoPaddingDialog(
            modifier = modifier,
            onDismissRequest = onClose,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProtonDialogTitle(
                    title = stringResource(R.string.autofill_cc_upgrade_dialog_title)
                )

                Text(
                    text = stringResource(R.string.autofill_cc_upgrade_dialog_message),
                    style = ProtonTheme.typography.defaultUnspecified
                )

                DialogCancelConfirmSection(
                    color = PassTheme.colors.interactionNormMajor2,
                    confirmText = stringResource(CompR.string.upgrade),
                    cancelText = stringResource(CoreR.string.presentation_close),
                    onDismiss = onClose,
                    onConfirm = onUpgrade
                )
            }
        }
    }
}
