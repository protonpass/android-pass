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

package proton.android.pass.features.sharing.manage.item.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.sharing.SharingNavigation
import proton.android.pass.features.sharing.manage.item.presentation.ManageItemEvent
import proton.android.pass.features.sharing.manage.item.presentation.ManageItemViewModel

@Composable
internal fun ManageItemScreen(
    modifier: Modifier = Modifier,
    refreshShareMembers: Boolean,
    onNavigateEvent: (SharingNavigation) -> Unit,
    viewModel: ManageItemViewModel = hiltViewModel()
) = with(viewModel) {
    val state by stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(refreshShareMembers) {
        if (refreshShareMembers) {
            onRefreshShareMembers()
        }
    }

    LaunchedEffect(state.event) {
        when (state.event) {
            ManageItemEvent.Idle -> Unit
            ManageItemEvent.OnShareManagementError -> onNavigateEvent(SharingNavigation.CloseScreen)
        }

        onConsumeEvent(state.event)
    }

    ManageItemContent(
        modifier = modifier,
        state = state,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                ManageItemUiEvent.OnBackClick -> {
                    onNavigateEvent(SharingNavigation.CloseScreen)
                }

                is ManageItemUiEvent.OnInviteToItemClick -> {
                    SharingNavigation.ShareItem(
                        shareId = uiEvent.shareId,
                        itemId = uiEvent.itemId
                    ).also(onNavigateEvent)
                }

                is ManageItemUiEvent.OnInviteToVaultClick -> {
                    SharingNavigation.ShareVault(
                        shareId = uiEvent.shareId
                    ).also(onNavigateEvent)
                }

                is ManageItemUiEvent.OnPendingInviteOptionsClick -> {
                    SharingNavigation.ManageItemInviteOptions(
                        shareId = uiEvent.shareId,
                        pendingInvite = uiEvent.pendingInvite
                    ).also(onNavigateEvent)
                }

                is ManageItemUiEvent.OnItemMemberOptionsClick -> {
                    SharingNavigation.ManageItemMemberOptions(
                        shareId = uiEvent.shareId,
                        memberShareId = uiEvent.member.shareId,
                        memberRole = uiEvent.member.role,
                        memberEmail = uiEvent.member.email
                    ).also(onNavigateEvent)
                }

                is ManageItemUiEvent.OnVaultMemberOptionsClick -> {
                    SharingNavigation.MemberOptions(
                        shareId = uiEvent.shareId,
                        destShareId = uiEvent.member.shareId,
                        memberRole = uiEvent.member.role,
                        destEmail = uiEvent.member.email
                    ).also(onNavigateEvent)
                }
            }
        }
    )
}
