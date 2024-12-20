/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.item.details.detailleave.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.component.ProtonDialogTitle
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.dialogs.DialogButton
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.item.details.R
import proton.android.pass.features.item.details.detailleave.presentation.ItemDetailsLeaveState
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun ItemDetailsLeaveContent(
    modifier: Modifier = Modifier,
    state: ItemDetailsLeaveState,
    onUiEvent: (ItemDetailsLeaveUiEvent) -> Unit
) = with(state) {
    NoPaddingDialog(
        modifier = modifier.padding(horizontal = Spacing.medium),
        backgroundColor = PassTheme.colors.backgroundStrong,
        onDismissRequest = {
            if (!isLoading) {
                onUiEvent(ItemDetailsLeaveUiEvent.OnDismiss)
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
        ) {
            ProtonDialogTitle(
                modifier = Modifier.padding(
                    start = Spacing.large,
                    top = Spacing.large,
                    bottom = Spacing.medium
                ),
                title = stringResource(id = R.string.item_details_leave_dialog_title)
            )

            Text.Body1Regular(
                modifier = Modifier.padding(horizontal = Spacing.large),
                text = stringResource(id = R.string.item_details_leave_dialog_message)
            )

            Row(
                modifier = Modifier
                    .align(alignment = Alignment.End)
                    .padding(
                        end = Spacing.medium,
                        bottom = Spacing.medium
                    )
            ) {
                DialogButton(
                    text = stringResource(id = CompR.string.action_cancel),
                    isEnabled = !isLoading,
                    onClick = { onUiEvent(ItemDetailsLeaveUiEvent.OnCancelClick) }
                )

                DialogButton(
                    text = stringResource(id = CompR.string.action_continue),
                    isEnabled = !isLoading,
                    isLoading = isLoading,
                    onClick = { onUiEvent(ItemDetailsLeaveUiEvent.OnContinueClick) }
                )
            }
        }
    }
}
