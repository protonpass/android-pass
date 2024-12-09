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

package proton.android.pass.features.sharing.manage.iteminviteoptions.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.features.sharing.R
import proton.android.pass.features.sharing.manage.iteminviteoptions.presentation.ManageItemInviteOptionsAction
import proton.android.pass.features.sharing.manage.iteminviteoptions.presentation.ManageItemInviteOptionsState
import me.proton.core.presentation.R as CoreR

@Composable
internal fun ManageItemInviteOptionsContent(
    modifier: Modifier = Modifier,
    state: ManageItemInviteOptionsState,
    onUiEvent: (ManageItemInviteOptionsUiEvent) -> Unit
) = with(state) {
    buildList {
        manageItemInviteOptionRow(
            titleRes = R.string.sharing_bottomsheet_resend_invite,
            iconRes = CoreR.drawable.ic_proton_paper_plane,
            isEnabled = action is ManageItemInviteOptionsAction.None,
            isLoading = action is ManageItemInviteOptionsAction.ResendInvite,
            onClick = { onUiEvent(ManageItemInviteOptionsUiEvent.OnResendInviteClick) }
        ).also(::add)

        manageItemInviteOptionRow(
            titleRes = R.string.sharing_bottomsheet_cancel_invite,
            iconRes = CoreR.drawable.ic_proton_circle_slash,
            isEnabled = action is ManageItemInviteOptionsAction.None,
            isLoading = action is ManageItemInviteOptionsAction.CancelInvite,
            onClick = { onUiEvent(ManageItemInviteOptionsUiEvent.OnCancelInviteClick) }
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
