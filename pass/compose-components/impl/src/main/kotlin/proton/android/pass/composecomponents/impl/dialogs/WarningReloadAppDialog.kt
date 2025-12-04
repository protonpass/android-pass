/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.composecomponents.impl.dialogs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.R


@Composable
fun WarningReloadAppDialog(
    modifier: Modifier = Modifier,
    defaultCheck: Boolean = false,
    onOkClick: (Boolean) -> Unit,
    onCancelClick: () -> Unit
) {
    val (isCheck, onChecked) = remember { mutableStateOf(defaultCheck) }

    BackHandler { onCancelClick() }

    NoPaddingDialog(
        modifier = modifier.padding(horizontal = Spacing.medium),
        backgroundColor = PassTheme.colors.backgroundWeak,
        onDismissRequest = onCancelClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.large),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.mediumSmall)
        ) {
            Text.Body1Regular(
                modifier = Modifier.padding(
                    top = Spacing.large
                ),
                text = stringResource(id = R.string.warning_dialog_reload_app_after_purchase_description)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChecked(!isCheck) }
                    .offset(x = -Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    colors = CheckboxDefaults.colors(
                        checkedColor = PassTheme.colors.interactionNormMajor1,
                        checkmarkColor = PassTheme.colors.textInvert
                    ),
                    checked = isCheck,
                    onCheckedChange = onChecked
                )

                Text.Body1Regular(
                    text = stringResource(id = R.string.warning_dialog_item_shared_vault_reminder),
                    color = PassTheme.colors.textNorm
                )
            }

            DialogCancelConfirmSection(
                modifier = Modifier.padding(vertical = Spacing.medium),
                onDismiss = onCancelClick,
                onConfirm = {
                    onOkClick(isCheck)
                },
                color = PassTheme.colors.interactionNormMajor2
            )
        }
    }
}
