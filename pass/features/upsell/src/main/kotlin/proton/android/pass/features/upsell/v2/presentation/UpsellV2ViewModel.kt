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

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.data.api.usecases.plan.ObservePlansWithPrice
import proton.android.pass.domain.plan.PlanWithPriceState
import proton.android.pass.features.upsell.v2.models.StepToDisplay
import proton.android.pass.features.upsell.v2.models.UpsellV2UiState
import proton.android.pass.features.upsell.v2.models.filterWelcomeOfferMonthly
import proton.android.pass.features.upsell.v2.models.filterWelcomeOfferYearly
import proton.android.pass.features.upsell.v2.models.toWelcomeOfferMonthlyUpsellUiModel
import proton.android.pass.features.upsell.v2.models.toWelcomeOfferYearlyUpsellUiModel
import proton.android.pass.features.upsell.v2.models.toYearlyUpsellUiModel
import proton.android.pass.features.upsell.v2.navigation.UpsellV2DisplayOnBoardingArg
import javax.inject.Inject

@HiltViewModel
class UpsellV2ViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    savedStateHandleProvider: SavedStateHandleProvider,
    private val observePlansWithPrice: ObservePlansWithPrice
) : ViewModel() {

    private val displayOnBoarding: Boolean = savedStateHandleProvider.get()
        .get<Boolean>(UpsellV2DisplayOnBoardingArg.key)
        ?: false

    private val _upsellV2UiState = MutableStateFlow(
        UpsellV2UiState(
            displayOnBoarding = displayOnBoarding,
            stepToDisplay = StepToDisplay.Loading
        )
    )
    val upsellV2UiState: StateFlow<UpsellV2UiState> = _upsellV2UiState

    init {
        viewModelScope.launch {
            updatePlans()
        }
    }

    private suspend fun updatePlans() {
        observePlansWithPrice()
            .collect { plans ->
                // we display only the first correct plans
                if (_upsellV2UiState.value.plans.isEmpty()) {
                    if (plans is PlanWithPriceState.PlansAvailable) {
                        if (displayOnBoarding) {
                            when {
                                // check if welcome offers exist first
                                plans.filterWelcomeOfferMonthly() != null -> {
                                    buildWelcomeMonthlyPlan(plans = plans)
                                }

                                // if no : check if welcome offer exist on yearly
                                plans.filterWelcomeOfferYearly() != null -> {
                                    buildWelcomeYearlyPlan(plans = plans)
                                }

                                // if no : else check annual
                                else -> {
                                    buildAnnualPlans(plans = plans)
                                }
                            }
                        } else { // always try to display two-columns view
                            buildAnnualPlans(plans = plans)
                        }
                    } else {
                        _upsellV2UiState.update {
                            it.copy(
                                stepToDisplay = when (plans) {
                                    is PlanWithPriceState.Loading -> {
                                        StepToDisplay.Loading
                                    }

                                    is PlanWithPriceState.Error,
                                    is PlanWithPriceState.NoPlan -> {
                                        StepToDisplay.NoPlans
                                    }

                                    else -> {
                                        // impossible
                                        StepToDisplay.Idle
                                    }
                                }
                            )
                        }
                    }
                }
            }
    }

    private fun buildWelcomeMonthlyPlan(plans: PlanWithPriceState.PlansAvailable) {
        plans.toWelcomeOfferMonthlyUpsellUiModel(
            context = context
        )?.let { monthly ->
            _upsellV2UiState.update {
                it.copy(
                    plans = persistentListOf(monthly),
                    stepToDisplay = StepToDisplay.WelcomeOfferMonthly
                )
            }
        } ?: {
            _upsellV2UiState.update {
                it.copy(
                    stepToDisplay = StepToDisplay.Next
                )
            }
        }
    }

    private fun buildWelcomeYearlyPlan(plans: PlanWithPriceState.PlansAvailable) {
        plans.toWelcomeOfferYearlyUpsellUiModel(
            context = context
        )?.let { yearly ->
            _upsellV2UiState.update {
                it.copy(
                    plans = persistentListOf(yearly),
                    stepToDisplay = StepToDisplay.WelcomeOfferYearly
                )
            }
        } ?: {
            _upsellV2UiState.update {
                it.copy(
                    stepToDisplay = StepToDisplay.Next
                )
            }
        }
    }

    private fun buildAnnualPlans(plans: PlanWithPriceState.PlansAvailable) {
        val annualPlans = plans
            .toYearlyUpsellUiModel()
            .toPersistentList()

        if (annualPlans.size == 2) {
            _upsellV2UiState.update {
                it.copy(
                    plans = annualPlans,
                    stepToDisplay = StepToDisplay.AnnualPlans
                )
            }
        } else {
            _upsellV2UiState.update {
                it.copy(
                    stepToDisplay = StepToDisplay.Next
                )
            }
        }
    }
}
