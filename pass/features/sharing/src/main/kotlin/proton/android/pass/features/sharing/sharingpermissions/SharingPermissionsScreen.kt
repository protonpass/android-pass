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

package proton.android.pass.features.sharing.sharingpermissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.sharing.SharingNavigation
import proton.android.pass.features.sharing.extensions.toShareRole

@Composable
fun SharingPermissionsScreen(
    modifier: Modifier = Modifier,
    onNavigateEvent: (SharingNavigation) -> Unit,
    viewModel: SharingPermissionsViewModel = hiltViewModel()
) = with(viewModel) {
    val state by stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (val event = state.event) {
            SharingPermissionsEvents.Unknown -> Unit

            is SharingPermissionsEvents.NavigateToSummary -> onNavigateEvent(
                SharingNavigation.Summary(shareId = event.shareId)
            )

            SharingPermissionsEvents.BackToHome -> onNavigateEvent(SharingNavigation.BackToHome)
        }

        clearEvent()
    }

    SharingPermissionsContent(
        modifier = modifier,
        state = state,
        onEvent = { uiEvent ->
            when (uiEvent) {
                SharingPermissionsUiEvent.OnBackClick -> {
                    onNavigateEvent(SharingNavigation.Back)
                }

                is SharingPermissionsUiEvent.OnPermissionChangeClick -> {
                    SharingNavigation.InviteToVaultEditPermissions(
                        email = uiEvent.address.address,
                        permission = uiEvent.address.permission.toShareRole()
                    ).also(onNavigateEvent)
                }

                SharingPermissionsUiEvent.OnSetAllPermissionsClick -> {
                    onNavigateEvent(SharingNavigation.InviteToVaultEditAllPermissions)
                }

                SharingPermissionsUiEvent.OnSubmit -> {
                    onPermissionsSubmit()
                }
            }
        }
    )
}
