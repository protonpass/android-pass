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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import proton.android.pass.common.api.None
import proton.android.pass.commonui.api.PassTopBarBackButtonType
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.loading.PassFullScreenLoading
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.crypto.api.extensions.toVault
import proton.android.pass.features.sharing.R
import proton.android.pass.features.sharing.manage.item.presentation.ManageItemState
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun ManageItemContent(
    modifier: Modifier,
    state: ManageItemState,
    onUiEvent: (ManageItemUiEvent) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            PassExtendedTopBar(
                backButton = PassTopBarBackButtonType.BackArrow,
                title = stringResource(R.string.shared_via_title),
                onUpClick = { onUiEvent(ManageItemUiEvent.OnBackClick) }
            )
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
                    if (state.hasItemMembers || state.hasItemPendingInvites) {
                        ManageItemMembersSection(
                            sectionTitle = stringResource(
                                id = R.string.shared_via_item_section_title,
                                pluralStringResource(
                                    id = CompR.plurals.users_count,
                                    count = state.itemMembers.size,
                                    state.itemMembers.size
                                )
                            ),
                            isItemSection = true,
                            isShareAdmin = state.share.isAdmin,
                            vaultOption = None,
                            shareItemsCount = state.itemsCount,
                            pendingInvites = state.itemPendingInvites,
                            members = state.itemMembers,
                            onPendingInviteMenuOptionsClick = { pendingInvite ->
                                ManageItemUiEvent.OnPendingInviteOptionsClick(
                                    shareId = state.share.id,
                                    pendingInvite = pendingInvite
                                ).also(onUiEvent)
                            },
                            onMemberMenuOptionsClick = { member ->
                                ManageItemUiEvent.OnMemberOptionsClick(
                                    shareId = state.share.id,
                                    member = member
                                ).also(onUiEvent)
                            },
                            onInviteMoreClick = {
                                ManageItemUiEvent.OnInviteToItemClick(
                                    shareId = state.share.id,
                                    itemId = state.itemId
                                ).also(onUiEvent)
                            }
                        )
                    }

                    if (state.hasVaultMembers || state.hasVaultPendingInvites) {
                        ManageItemMembersSection(
                            sectionTitle = stringResource(
                                id = R.string.shared_via_vault_section_title,
                                pluralStringResource(
                                    id = CompR.plurals.members_count,
                                    count = state.vaultMembers.size,
                                    state.vaultMembers.size
                                )
                            ),
                            isItemSection = false,
                            isShareAdmin = state.share.isAdmin,
                            vaultOption = state.share.toVault(),
                            shareItemsCount = state.itemsCount,
                            pendingInvites = state.vaultPendingInvites,
                            members = state.vaultMembers,
                            onPendingInviteMenuOptionsClick = { pendingInvite ->
                                ManageItemUiEvent.OnPendingInviteOptionsClick(
                                    shareId = state.share.id,
                                    pendingInvite = pendingInvite
                                ).also(onUiEvent)
                            },
                            onMemberMenuOptionsClick = { member ->
                                ManageItemUiEvent.OnMemberOptionsClick(
                                    shareId = state.share.id,
                                    member = member
                                ).also(onUiEvent)
                            },
                            onInviteMoreClick = {
                                ManageItemUiEvent.OnInviteToVaultClick(
                                    shareId = state.share.id
                                ).also(onUiEvent)
                            }
                        )
                    }
                }
            }
        }
    }
}
