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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.payment.presentation.viewmodel.ProtonPaymentEvent
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.RefreshUserAccess
import proton.android.pass.data.api.usecases.plan.ObservePlansWithPrice
import proton.android.pass.domain.Plan
import proton.android.pass.domain.plan.PlanWithPriceState
import proton.android.pass.features.upsell.v2.models.StepToDisplay
import proton.android.pass.features.upsell.v2.models.UpsellV2UiState
import proton.android.pass.features.upsell.v2.models.filterWelcomeOfferMonthly
import proton.android.pass.features.upsell.v2.models.filterWelcomeOfferYearly
import proton.android.pass.features.upsell.v2.models.toWelcomeOfferMonthlyUpsellUiModel
import proton.android.pass.features.upsell.v2.models.toWelcomeOfferYearlyUpsellUiModel
import proton.android.pass.features.upsell.v2.models.toYearlyUpsellUiModel
import proton.android.pass.features.upsell.v2.navigation.UpsellV2DisplayOnBoardingArg
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.ToastManager
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import me.proton.core.plan.presentation.R as PaymentR

@HiltViewModel
class UpsellV2ViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    savedStateHandleProvider: SavedStateHandleProvider,
    private val observePlansWithPrice: ObservePlansWithPrice,
    private val accountManager: AccountManager,
    private val getUserPlan: GetUserPlan,
    private val refreshUserAccess: RefreshUserAccess,
    private val toastManager: ToastManager
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

    private suspend fun getPrimaryUserIdOrNull() = accountManager.getPrimaryUserId().firstOrNull()

    internal fun manageError(error: ProtonPaymentEvent.Error) {
        PassLogger.w(TAG, "Error during payment : $error")

        when (error) {
            ProtonPaymentEvent.Error.GoogleProductDetailsNotFound -> {
                toastManager.showToast(PaymentR.string.payments_error_google_prices)
            }
            ProtonPaymentEvent.Error.UserCancelled -> {
                // do nothing
            }

            else -> {
                toastManager.showToast(PaymentR.string.payments_general_error)
            }
        }

        if (error !is ProtonPaymentEvent.Error.UserCancelled) {
            _upsellV2UiState.update {
                it.copy(
                    stepToDisplay = StepToDisplay.Next
                )
            }
        }
    }

    internal fun upgrade() = viewModelScope.launch {
        getPrimaryUserIdOrNull()?.let { userId ->
            _upsellV2UiState.update {
                it.copy(
                    displayLoaderDuringPurchase = true
                )
            }

            viewModelScope.launch {
                safeRunCatching {
                    val maxAttempts = 3
                    val baseDelay = 2.seconds
                    val previousPlan: Plan? = getUserPlan(userId).firstOrNull()

                    repeat(maxAttempts) { attempt ->
                        delay(baseDelay * (attempt + 1))
                        runCatching { refreshUserAccess(userId) }
                            .onSuccess { PassLogger.i(TAG, "Plan refreshed") }
                            .onFailure {
                                PassLogger.w(TAG, "Error refreshing plan")
                                PassLogger.w(TAG, it)
                            }
                        val currentPlan: Plan? = getUserPlan(userId).firstOrNull()
                        if (previousPlan?.internalName != currentPlan?.internalName) {
                            _upsellV2UiState.update {
                                it.copy(
                                    stepToDisplay = StepToDisplay.Next
                                )
                            }
                            return@launch
                        }
                    }

                    // if it's too long, go to next screen.
                    _upsellV2UiState.update {
                        it.copy(
                            stepToDisplay = StepToDisplay.Next
                        )
                    }
                }
            }
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

private const val TAG = "UpsellV2ViewModel"
