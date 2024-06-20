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

package proton.android.pass.features.secure.links.list.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.secure.links.list.presentation.SecureLinksListViewModel
import proton.android.pass.features.secure.links.overview.navigation.SecureLinksOverviewNavScope
import proton.android.pass.features.secure.links.shared.navigation.SecureLinksNavDestination

@Composable
fun SecureLinksListScreen(
    onNavigated: (SecureLinksNavDestination) -> Unit,
    viewModel: SecureLinksListViewModel = hiltViewModel()
) = with(viewModel) {
    val state by state.collectAsStateWithLifecycle()

    SecureLinksListContent(
        state = state,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                SecureLinksListUiEvent.OnBackClicked -> {
                   onNavigated(SecureLinksNavDestination.Profile)
                }

                is SecureLinksListUiEvent.OnCellClicked -> {
                    SecureLinksNavDestination.SecureLinkOverview(
                        secureLinkId = uiEvent.secureLinkId,
                        scope = SecureLinksOverviewNavScope.SecureLinksList
                    ).also(onNavigated)
                }

                is SecureLinksListUiEvent.OnCellOptionsClicked -> {
                   SecureLinksNavDestination.SecureLinksListMenu(
                       secureLinkId = uiEvent.secureLinkId
                   ).also(onNavigated)
                }
            }
        }
    )
}
