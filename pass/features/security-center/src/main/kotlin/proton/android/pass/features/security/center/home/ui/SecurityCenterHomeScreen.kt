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

package proton.android.pass.features.security.center.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.security.center.home.navigation.SecurityCenterHomeNavDestination
import proton.android.pass.features.security.center.home.presentation.SecurityCenterHomeViewModel

@Composable
fun SecurityCenterHomeScreen(
    onNavigated: (SecurityCenterHomeNavDestination) -> Unit,
    viewModel: SecurityCenterHomeViewModel = hiltViewModel()
) = with(viewModel) {
    val state by state.collectAsStateWithLifecycle()

    SecurityCenterHomeContent(
        state = state,
        onUiEvent = { uiEvent ->
            when (uiEvent) {
                SecurityCenterHomeUiEvent.OnShowDataBreaches -> {
                    onNavigated(SecurityCenterHomeNavDestination.DarkWebMonitoring)
                }

                SecurityCenterHomeUiEvent.OnShowSentinelBottomSheet -> {
                    onNavigated(SecurityCenterHomeNavDestination.Sentinel)
                }

                SecurityCenterHomeUiEvent.OnShowMissingSecondAuthFactors -> {
                    onNavigated(SecurityCenterHomeNavDestination.MissingTFA)
                }

                SecurityCenterHomeUiEvent.OnShowReusedPasswords -> {
                    onNavigated(SecurityCenterHomeNavDestination.ReusedPasswords)
                }

                SecurityCenterHomeUiEvent.OnShowWeakPasswords -> {
                    onNavigated(SecurityCenterHomeNavDestination.WeakPasswords)
                }

                is SecurityCenterHomeUiEvent.OnUpsell -> {
                    onNavigated(SecurityCenterHomeNavDestination.Upsell(uiEvent.paidFeature))
                }

                SecurityCenterHomeUiEvent.OnShowExcludedItems -> {
                    onNavigated(SecurityCenterHomeNavDestination.ExcludedItems)
                }
            }
        }
    )
}
