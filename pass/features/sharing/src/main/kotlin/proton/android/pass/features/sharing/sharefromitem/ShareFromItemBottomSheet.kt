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

package proton.android.pass.features.sharing.sharefromitem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.domain.features.PaidFeature
import proton.android.pass.features.sharing.SharingNavigation

@Composable
fun ShareFromItemBottomSheet(
    modifier: Modifier = Modifier,
    onNavigateEvent: (SharingNavigation) -> Unit,
    viewModel: ShareFromItemViewModel = hiltViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (state.event) {
            ShareFromItemNavEvent.Unknown -> Unit
            ShareFromItemNavEvent.MoveToSharedVault -> {
                onNavigateEvent(SharingNavigation.MoveItemToSharedVault)
            }
        }

        viewModel.onEventConsumed(state.event)
    }

    ShareFromItemContent(
        modifier = modifier,
        state = state,
        onEvent = { event ->
            when (event) {
                ShareFromItemEvent.CreateNewVault -> {
                    onNavigateEvent(
                        SharingNavigation.CreateVaultAndMoveItem(
                            shareId = state.shareId,
                            itemId = state.itemId
                        )
                    )
                }

                ShareFromItemEvent.MoveToSharedVault -> {
                    viewModel.moveItemToSharedVault()
                }

                ShareFromItemEvent.ShareVault -> {
                    onNavigateEvent(SharingNavigation.ShareVault(state.shareId))
                }

                ShareFromItemEvent.ShareSecureLink -> SharingNavigation.ShareItemLink(
                    shareId = state.shareId,
                    itemId = state.itemId
                ).also(onNavigateEvent)

                ShareFromItemEvent.Upgrade -> {
                    onNavigateEvent(SharingNavigation.Upgrade)
                }

                ShareFromItemEvent.UpsellSecureLink -> SharingNavigation.Upsell(
                    paidFeature = PaidFeature.SecureLinks
                ).also(onNavigateEvent)

                ShareFromItemEvent.ManageSharedVault -> SharingNavigation.ManageSharedVault(
                    sharedVaultId = state.shareId
                ).also(onNavigateEvent)

                ShareFromItemEvent.ShareItem -> SharingNavigation.ShareItem(
                    shareId = state.shareId,
                    itemId = state.itemId
                ).also(onNavigateEvent)

                ShareFromItemEvent.ManageSharedItem -> SharingNavigation.ManageItem(
                    shareId = state.shareId,
                    itemId = state.itemId
                ).also(onNavigateEvent)

                ShareFromItemEvent.UpsellItemSharing -> SharingNavigation.Upsell(
                    paidFeature = PaidFeature.ItemSharing
                ).also(onNavigateEvent)
            }
        }
    )
}
