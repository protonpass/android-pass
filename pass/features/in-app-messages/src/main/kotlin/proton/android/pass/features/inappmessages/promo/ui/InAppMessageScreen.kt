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

package proton.android.pass.features.inappmessages.promo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.inappmessages.bottomsheet.navigation.InAppMessageModalDestination
import proton.android.pass.features.inappmessages.bottomsheet.presentation.InAppMessageModalState
import proton.android.pass.features.inappmessages.bottomsheet.presentation.InAppMessageModalViewModel

@Composable
fun InAppMessagePromoScreen(
    modifier: Modifier = Modifier,
    viewModel: InAppMessageModalViewModel = hiltViewModel(),
    onNavigate: (InAppMessageModalDestination) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        is InAppMessageModalState.Success -> {
            val successState = state as InAppMessageModalState.Success
            LaunchedEffect(Unit) {
                viewModel.onInAppMessageDisplayed(successState.inAppMessage.key)
            }

            InAppMessagePromoContent(
                modifier = modifier,
                inAppMessage = successState.inAppMessage,
                onClose = {
                    onNavigate(InAppMessageModalDestination.CloseBottomsheet)
                }
            )
        }
        is InAppMessageModalState.Loading -> {
        }
        is InAppMessageModalState.Error -> {
            onNavigate(InAppMessageModalDestination.CloseBottomsheet)
        }
    }
}

