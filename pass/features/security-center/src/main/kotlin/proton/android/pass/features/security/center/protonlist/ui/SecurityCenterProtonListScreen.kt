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

package proton.android.pass.features.security.center.protonlist.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.security.center.addressoptions.navigation.AddressOptionsType.DisableMonitoring
import proton.android.pass.features.security.center.addressoptions.navigation.AddressOptionsType.EnableMonitoring
import proton.android.pass.features.security.center.addressoptions.navigation.GlobalMonitorAddressType
import proton.android.pass.features.security.center.protonlist.navigation.SecurityCenterProtonListNavDestination
import proton.android.pass.features.security.center.protonlist.presentation.SecurityCenterProtonListEvent
import proton.android.pass.features.security.center.protonlist.presentation.SecurityCenterProtonListViewModel

@Composable
fun SecurityCenterProtonListScreen(
    onNavigated: (SecurityCenterProtonListNavDestination) -> Unit,
    viewModel: SecurityCenterProtonListViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (state.event) {
            SecurityCenterProtonListEvent.Idle -> {}
        }
        viewModel.onEventConsumed(state.event)
    }
    SecurityCenterProtonListContent(state = state) { event ->
        when (event) {
            SecurityCenterProtonListUiEvent.Back ->
                onNavigated(SecurityCenterProtonListNavDestination.Back)

            is SecurityCenterProtonListUiEvent.EmailBreachClick ->
                onNavigated(
                    SecurityCenterProtonListNavDestination.OnEmailClick(
                        event.id,
                        event.email
                    )
                )

            SecurityCenterProtonListUiEvent.OptionsClick -> {
                onNavigated(
                    SecurityCenterProtonListNavDestination.OnOptionsClick(
                        GlobalMonitorAddressType.Proton,
                        if (state.isGlobalMonitorEnabled) DisableMonitoring else EnableMonitoring
                    )
                )
            }
        }
    }
}
