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

package proton.android.pass.features.sharing.sharingsummary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.sharing.SharingNavigation

@Composable
fun SharingSummaryScreen(
    modifier: Modifier = Modifier,
    onNavigateEvent: (SharingNavigation) -> Unit,
    viewModel: SharingSummaryViewModel = hiltViewModel()
) = with(viewModel) {
    val state by stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (val event = state.event) {
            SharingSummaryEvent.Idle -> Unit

            SharingSummaryEvent.OnGoHome -> onNavigateEvent(SharingNavigation.BackToHome)

            is SharingSummaryEvent.OnSharingItemSuccess -> SharingNavigation.ItemDetails(
                itemCategory = event.itemCategory
            ).also(onNavigateEvent)

            SharingSummaryEvent.OnSharingVaultError -> onNavigateEvent(SharingNavigation.InviteError)

            is SharingSummaryEvent.OnSharingVaultSuccess -> SharingNavigation.ManageVault(
                shareId = event.shareId
            ).also(onNavigateEvent)
        }

        onConsumeEvent(state.event)
    }

    SharingSummaryContent(
        modifier = modifier,
        state = state,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                SharingSummaryUiEvent.OnBackClick -> {
                    onNavigateEvent(SharingNavigation.Back)
                }

                is SharingSummaryUiEvent.OnShareItemClick -> {
                    onShareItem(
                        itemId = uiEvent.itemId,
                        itemCategory = uiEvent.itemCategory
                    )
                }

                SharingSummaryUiEvent.OnShareVaultClick -> {
                    onShareVault()
                }
            }
        }
    )
}
