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

package proton.android.pass.features.sharing.manage.itemmemberoptions.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.domain.ShareRole
import proton.android.pass.features.sharing.R
import proton.android.pass.features.sharing.manage.itemmemberoptions.presentation.ManageItemMemberOptionsAction
import proton.android.pass.features.sharing.manage.itemmemberoptions.presentation.ManageItemMemberOptionsState
import me.proton.core.presentation.R as CoreR

@Composable
internal fun ManageItemMemberOptionsContent(
    modifier: Modifier = Modifier,
    state: ManageItemMemberOptionsState,
    onUiEvent: (ManageItemMemberOptionsUiEvent) -> Unit
) = with(state) {
    buildList {
        manageItemMemberOptionRow(
            titleRes = if (state.isRenameAdminToManagerEnabled) {
                R.string.sharing_can_manage_V2
            } else {
                R.string.sharing_can_manage
            },
            subtitleRes = R.string.sharing_bottomsheet_item_admin_subtitle,
            iconRes = CoreR.drawable.ic_proton_key,
            isSelected = memberShareRole is ShareRole.Admin,
            isEnabled = state.action is ManageItemMemberOptionsAction.None,
            isLoading = action is ManageItemMemberOptionsAction.SetAdmin,
            onClick = {
                ManageItemMemberOptionsUiEvent.OnChangeMemberRoleClick(
                    newMemberRole = ShareRole.Admin
                ).also(onUiEvent)
            }
        ).also(::add)

        manageItemMemberOptionRow(
            titleRes = R.string.sharing_can_edit,
            subtitleRes = R.string.sharing_bottomsheet_item_editor_subtitle,
            iconRes = CoreR.drawable.ic_proton_pencil,
            isSelected = memberShareRole is ShareRole.Write,
            isEnabled = action is ManageItemMemberOptionsAction.None,
            isLoading = action is ManageItemMemberOptionsAction.SetEditor,
            onClick = {
                ManageItemMemberOptionsUiEvent.OnChangeMemberRoleClick(
                    newMemberRole = ShareRole.Write
                ).also(onUiEvent)
            }
        ).also(::add)

        manageItemMemberOptionRow(
            titleRes = R.string.sharing_can_view,
            subtitleRes = R.string.sharing_bottomsheet_item_viewer_subtitle,
            iconRes = CoreR.drawable.ic_proton_eye,
            isSelected = memberShareRole is ShareRole.Read,
            isEnabled = action is ManageItemMemberOptionsAction.None,
            isLoading = action is ManageItemMemberOptionsAction.SetViewer,
            onClick = {
                ManageItemMemberOptionsUiEvent.OnChangeMemberRoleClick(
                    newMemberRole = ShareRole.Read
                ).also(onUiEvent)
            }
        ).also(::add)

        manageItemMemberOptionRow(
            titleRes = R.string.sharing_bottomsheet_remove_access,
            subtitleRes = null,
            iconRes = CoreR.drawable.ic_proton_circle_slash,
            isSelected = false,
            isEnabled = state.action is ManageItemMemberOptionsAction.None,
            isLoading = action is ManageItemMemberOptionsAction.RevokeAccess,
            onClick = {
                onUiEvent(ManageItemMemberOptionsUiEvent.OnRevokeMemberAccessClick)
            }
        ).also(::add)
    }.let { items ->
        BottomSheetItemList(
            modifier = modifier.bottomSheet(),
            items = items
                .withDividers()
                .toPersistentList()
        )
    }
}
