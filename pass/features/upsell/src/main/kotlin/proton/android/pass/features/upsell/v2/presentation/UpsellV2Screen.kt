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

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.payment.presentation.viewmodel.ProtonPaymentEvent
import me.proton.core.plan.presentation.ui.StartUnredeemedPurchase
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

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            viewModel.upgrade()
        }
    }

    UpsellV2Content(
        modifier = modifier,
        uiState = state,
        onPaymentCallback = {
            when (it) {
                is ProtonPaymentEvent.GiapSuccess -> {
                    viewModel.upgrade()
                }

                is ProtonPaymentEvent.Error -> {
                    when (it) {
                        is ProtonPaymentEvent.Error.GiapUnredeemed -> {
                            launcher.launch(
                                input = StartUnredeemedPurchase.createIntent(context, Unit).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            )
                        }

                        else -> {
                            viewModel.manageError(it)
                        }
                    }
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

private const val TAG = "UpsellV2Screen"
