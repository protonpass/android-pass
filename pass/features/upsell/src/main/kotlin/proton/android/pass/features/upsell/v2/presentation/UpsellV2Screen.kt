/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.upsell.v2.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.payment.presentation.viewmodel.ProtonPaymentEvent
import proton.android.pass.features.upsell.v2.models.StepToDisplay

@Composable
fun UpsellV2Screen(
    modifier: Modifier = Modifier,
    viewModel: UpsellV2ViewModel = hiltViewModel(),
    onPlanFinished: (Boolean) -> Unit,
    onSkip: (Boolean) -> Unit,
    onNavigateBack: (Boolean) -> Unit
) {
    val state by viewModel.upsellV2UiState.collectAsStateWithLifecycle()

    BackHandler { onNavigateBack(state.displayOnBoarding) }

    LaunchedEffect(state.stepToDisplay) {
        when (state.stepToDisplay) {
            StepToDisplay.Next -> onPlanFinished(state.displayOnBoarding)
            // if no plans during onboarding do not informs user and go to next screen
            StepToDisplay.NoPlans -> if (state.displayOnBoarding) onPlanFinished(true) else Unit
            else -> Unit
        }
    }

    UpsellV2Content(
        modifier = modifier,
        uiState = state,
        onPaymentCallback = {
            when (it) {
                is ProtonPaymentEvent.GiapSuccess -> {
                    onPlanFinished(state.displayOnBoarding)
                }

                is ProtonPaymentEvent.Error -> {
                    onPlanFinished(state.displayOnBoarding)
                }

                else -> {
                    // nothing to do : stay where the user is
                }
            }
        },
        onSkipButtonClick = {
            onSkip(state.displayOnBoarding)
        }
    )
}
