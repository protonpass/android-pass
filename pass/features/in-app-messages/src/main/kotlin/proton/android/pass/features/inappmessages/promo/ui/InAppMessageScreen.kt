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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.BrowserUtils
import proton.android.pass.features.inappmessages.navigation.InAppMessageDestination
import proton.android.pass.features.inappmessages.promo.presentation.InAppMessagePromoEvent
import proton.android.pass.features.inappmessages.promo.presentation.InAppMessagePromoState
import proton.android.pass.features.inappmessages.promo.presentation.InAppMessagePromoViewModel

@Composable
fun InAppMessagePromoScreen(
    modifier: Modifier = Modifier,
    viewModel: InAppMessagePromoViewModel = hiltViewModel(),
    onNavigate: (InAppMessageDestination) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    val event = (state as? InAppMessagePromoState.Success)?.event
    LaunchedEffect(event) {
        when (event) {
            is InAppMessagePromoEvent.OnCTAClicked -> {
                BrowserUtils.openWebsite(context, event.website)
                onNavigate(InAppMessageDestination.CloseScreen)
            }
            is InAppMessagePromoEvent.OnInternalCTAClicked ->
                onNavigate(InAppMessageDestination.DeepLink(event.deepLink, false))
            InAppMessagePromoEvent.OnClose ->
                onNavigate(InAppMessageDestination.CloseScreen)

            InAppMessagePromoEvent.Idle,
            null -> Unit
        }
        event?.let { viewModel.onConsumeEvent(it) }
    }

    when (state) {
        is InAppMessagePromoState.Success -> {
            val successState = state as InAppMessagePromoState.Success

            InAppMessagePromoContent(
                modifier = modifier,
                inAppMessage = successState.inAppMessage,
                themePreference = successState.themePreference,
                onExternalCTAClick = {
                    viewModel.onCTAClicked(successState.inAppMessage.key, it)
                },
                onInternalCTAClick = {
                    viewModel.onInternalCTAClicked(successState.inAppMessage.key, it)
                },
                onMinimize = {
                    viewModel.onClose(successState.inAppMessage.key)
                },
                onDontShowAgain = {
                    viewModel.onDontShowAgain(successState.inAppMessage.key)
                }
            )
        }

        is InAppMessagePromoState.Loading -> {
        }

        is InAppMessagePromoState.Error -> onNavigate(InAppMessageDestination.CloseScreen)
    }
}

