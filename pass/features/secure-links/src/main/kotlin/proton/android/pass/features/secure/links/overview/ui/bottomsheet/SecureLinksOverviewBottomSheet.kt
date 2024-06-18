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

package proton.android.pass.features.secure.links.overview.ui.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.secure.links.overview.presentation.SecureLinksOverviewEvent
import proton.android.pass.features.secure.links.overview.presentation.SecureLinksOverviewViewModel
import proton.android.pass.features.secure.links.overview.ui.shared.events.handleSecureLinksOverviewUiEvent
import proton.android.pass.features.secure.links.shared.navigation.SecureLinksNavDestination

@Composable
internal fun SecureLinksOverviewBottomSheet(
    onNavigated: (SecureLinksNavDestination) -> Unit,
    viewModel: SecureLinksOverviewViewModel = hiltViewModel()
) = with(viewModel) {
    val state by state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(key1 = state.event) {
        when (state.event) {
            SecureLinksOverviewEvent.Idle -> {}

            SecureLinksOverviewEvent.OnSecureLinkDeleted -> SecureLinksNavDestination.Back(
                comesFromBottomSheet = true
            ).also(onNavigated)
        }

        onEventConsumed(event = state.event)
    }

    SecureLinksOverviewBottomSheetContent(
        state = state,
        onUiEvent = { uiEvent ->
            handleSecureLinksOverviewUiEvent(
                uiEvent = uiEvent,
                secureLinkUrl = state.secureLinkUrl,
                onNavigated = onNavigated,
                onLinkCopied = ::onLinkCopied,
                onLinkDeleted = ::onLinkDeleted,
                context = context
            )
        }
    )
}
