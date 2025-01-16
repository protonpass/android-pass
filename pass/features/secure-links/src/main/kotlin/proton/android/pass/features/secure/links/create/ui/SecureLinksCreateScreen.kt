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

package proton.android.pass.features.secure.links.create.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.secure.links.create.presentation.SecureLinksCreateEvent
import proton.android.pass.features.secure.links.create.presentation.SecureLinksCreateViewModel
import proton.android.pass.features.secure.links.overview.navigation.SecureLinksOverviewNavScope
import proton.android.pass.features.secure.links.shared.navigation.SecureLinksNavDestination

@Composable
fun SecureLinksCreateScreen(
    onNavigated: (SecureLinksNavDestination) -> Unit,
    viewModel: SecureLinksCreateViewModel = hiltViewModel()
) = with(viewModel) {
    val state by state.collectAsStateWithLifecycle()

    var shouldDisplayExpirationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = state.event) {
        when (val event = state.event) {
            SecureLinksCreateEvent.Idle -> {}
            is SecureLinksCreateEvent.OnLinkGenerated -> SecureLinksNavDestination.SecureLinkOverview(
                secureLinkId = event.secureLinkId,
                scope = SecureLinksOverviewNavScope.SecureLinksGeneration
            ).also(onNavigated)
        }
    }

    SecureLinksCreateContent(
        state = state,
        shouldDisplayExpirationDialog = shouldDisplayExpirationDialog,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                SecureLinksCreateUiEvent.OnBackArrowClicked -> {
                    onNavigated(SecureLinksNavDestination.CloseScreen)
                }

                SecureLinksCreateUiEvent.OnSetExpirationClicked -> {
                    shouldDisplayExpirationDialog = true
                }

                SecureLinksCreateUiEvent.OnEnableMaxViewsClicked -> {
                    onMaxViewsEnabled()
                }

                SecureLinksCreateUiEvent.OnDisableMaxViewsClicked -> {
                    onMaxViewsDisabled()
                }

                SecureLinksCreateUiEvent.OnDecreaseMaxViewsClicked -> {
                    onMaxViewsDecreased()
                }

                SecureLinksCreateUiEvent.OnIncreaseMaxViewsClicked -> {
                    onMaxViewsIncreased()
                }

                SecureLinksCreateUiEvent.OnGenerateLinkClicked -> {
                    onGenerateSecureLink()
                }

                SecureLinksCreateUiEvent.OnExpirationDialogDismissed -> {
                    shouldDisplayExpirationDialog = false
                }

                is SecureLinksCreateUiEvent.OnExpirationSelected -> {
                    onExpirationSelected(uiEvent.newExpiration)
                }
            }
        }
    )
}
