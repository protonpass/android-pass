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

package proton.android.pass.features.sharing.accept

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.features.sharing.SharingNavigation

@Composable
fun AcceptInviteBottomSheet(
    modifier: Modifier = Modifier,
    onNavigateEvent: (SharingNavigation) -> Unit,
    viewModel: AcceptInviteViewModel = hiltViewModel()
) = with(viewModel) {
    val state by stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (val event = state.event) {
            AcceptInviteEvent.Idle -> Unit

            AcceptInviteEvent.Close -> {
                onNavigateEvent(SharingNavigation.BackToHome)
            }

            is AcceptInviteEvent.OnItemInviteAcceptSuccess -> {
                SharingNavigation.SharedItemDetails(
                    shareId = event.shareId,
                    itemId = event.itemId,
                    itemCategory = event.itemCategory
                ).also(onNavigateEvent)
            }

            is AcceptInviteEvent.OnVaultInviteAcceptSuccess -> {
                SharingNavigation.ManageSharedVault(
                    sharedVaultId = event.shareId
                ).also(onNavigateEvent)
            }
        }

        onConsumeEvent(state.event)
    }

    AcceptInviteContent(
        modifier = modifier
            .fillMaxWidth()
            .bottomSheet(),
        state = state,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                is AcceptInviteUiEvent.OnAcceptInvitationClick -> {
                    onAcceptInvite(shareType = uiEvent.shareType)
                }

                AcceptInviteUiEvent.OnRejectInvitationClick -> {
                    onRejectInvite()
                }
            }
        }
    )
}
