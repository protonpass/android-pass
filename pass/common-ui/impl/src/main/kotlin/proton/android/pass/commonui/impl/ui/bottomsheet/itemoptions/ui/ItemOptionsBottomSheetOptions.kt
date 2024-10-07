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

package proton.android.pass.commonui.impl.ui.bottomsheet.itemoptions.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.commonui.impl.R
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.domain.items.ItemOption

@Composable
internal fun ItemOptionsBottomSheetOptions(
    modifier: Modifier = Modifier,
    itemOptions: ImmutableList<ItemOption>,
    isLoading: Boolean,
    onUiEvent: (ItemOptionsBottomSheetUiEvent) -> Unit
) {
    itemOptions.map { itemOption ->
        when (itemOption) {
            is ItemOption.CopyEmail -> copyToClipboard(
                text = stringResource(id = R.string.bottomsheet_copy_email),
                onClick = {
                    ItemOptionsBottomSheetUiEvent.OnCopyEmailClicked(
                        email = itemOption.email
                    ).also(onUiEvent)
                }
            )

            is ItemOption.CopyPassword -> copyToClipboard(
                text = stringResource(id = R.string.bottomsheet_copy_password),
                onClick = {
                    ItemOptionsBottomSheetUiEvent.OnCopyPasswordClicked(
                        encryptedPassword = itemOption.encryptedPassword
                    ).also(onUiEvent)
                }
            )

            is ItemOption.CopyUsername -> copyToClipboard(
                text = stringResource(id = R.string.bottomsheet_copy_username),
                onClick = {
                    ItemOptionsBottomSheetUiEvent.OnCopyUsernameClicked(
                        username = itemOption.username
                    ).also(onUiEvent)
                }
            )

            ItemOption.MoveToTrash -> moveToTrash(
                isLoading = isLoading,
                onClick = {
                    ItemOptionsBottomSheetUiEvent.OnMoveToTrashClicked
                        .also(onUiEvent)
                }
            )
        }
    }.let { bottomSheetItems ->
        BottomSheetItemList(
            modifier = modifier.bottomSheet(),
            items = bottomSheetItems.withDividers().toPersistentList()
        )
    }
}
