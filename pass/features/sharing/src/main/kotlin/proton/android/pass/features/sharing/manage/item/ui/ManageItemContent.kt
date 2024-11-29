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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTopBarBackButtonType
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.loading.PassFullScreenLoading
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.features.sharing.R
import proton.android.pass.features.sharing.manage.item.presentation.ManageItemState

@Composable
internal fun ManageItemContent(
    modifier: Modifier,
    state: ManageItemState,
    onUiEvent: (ManageItemUiEvent) -> Unit
) = with(state) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PassExtendedTopBar(
                backButton = PassTopBarBackButtonType.BackArrow,
                title = stringResource(R.string.share_with_title),
                onUpClick = { onUiEvent(ManageItemUiEvent.OnBackClick) }
            )
        },
        bottomBar = {
            when (state) {
                ManageItemState.Loading -> Unit
                is ManageItemState.Success -> {
                    ManageItemBottomBar(
                        share = state.share,
                        isLoading = state.isLoading,
                        onUiEvent = onUiEvent
                    )
                }
            }
        }
    ) { innerPaddingValues ->
        when (state) {
            ManageItemState.Loading -> {
                PassFullScreenLoading()
            }

            is ManageItemState.Success -> {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues = innerPaddingValues)
                        .padding(
                            start = Spacing.medium,
                            top = Spacing.large,
                            end = Spacing.medium
                        ),
                    verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
                ) {
                    if (state.hasPendingInvites) {
                        ManageItemPendingInvitesSection(
                            pendingInvites = state.pendingInvites,
                            onMenuOptionsClick = { pendingInvite ->
                                ManageItemUiEvent.OnPendingInviteOptionsClick(
                                    shareId = state.share.id,
                                    pendingInvite = pendingInvite
                                ).also(onUiEvent)
                            }
                        )
                    }

                    if (state.hasMembers) {
                        ManageItemMembersSection(
                            share = state.share,
                            members = state.members,
                            onMenuOptionsClick = { member ->
                                ManageItemUiEvent.OnMemberOptionsClick(
                                    shareId = state.share.id,
                                    member = member
                                ).also(onUiEvent)
                            }
                        )
                    }
                }
            }
        }
    }
}
