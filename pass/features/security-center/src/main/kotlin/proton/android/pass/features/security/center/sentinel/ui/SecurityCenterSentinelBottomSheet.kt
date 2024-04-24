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

package proton.android.pass.features.security.center.sentinel.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.BrowserUtils
import proton.android.pass.features.security.center.sentinel.navigation.SecurityCenterSentinelDestination
import proton.android.pass.features.security.center.sentinel.presentation.SecurityCenterSentinelEvent
import proton.android.pass.features.security.center.sentinel.presentation.SecurityCenterSentinelViewModel

private const val SENTINEL_LEARN_MORE_LINK = "https://proton.me/support/proton-sentinel"

@Composable
fun SecurityCenterSentinelBottomSheet(
    onNavigated: (SecurityCenterSentinelDestination) -> Unit,
    viewModel: SecurityCenterSentinelViewModel = hiltViewModel()
) = with(viewModel) {
    val state by state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(key1 = state.event) {
        when (state.event) {
            SecurityCenterSentinelEvent.Idle -> {

            }

            SecurityCenterSentinelEvent.OnLearnMore -> {
                BrowserUtils.openWebsite(context, SENTINEL_LEARN_MORE_LINK)
            }

            SecurityCenterSentinelEvent.OnSentinelEnableError,
            SecurityCenterSentinelEvent.OnSentinelEnableSuccess -> {
                onNavigated(SecurityCenterSentinelDestination.Dismiss)
            }
        }

        onEventConsumed(state.event)
    }

    SecurityCenterSentinelBottomSheetContent(
        state = state,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                SecurityCenterSentinelUiEvent.OnEnableSentinel -> onEnableSentinel()
                SecurityCenterSentinelUiEvent.OnLearnMore -> onLearnMore()
                SecurityCenterSentinelUiEvent.OnUpsell -> onNavigated(SecurityCenterSentinelDestination.Upsell)
            }
        }
    )
}
