package proton.android.pass.featureaccount.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import me.proton.core.payment.domain.PaymentManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.UserPlan
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlags
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    ffPreferencesRepository: FeatureFlagsPreferencesRepository,
    observeCurrentUser: ObserveCurrentUser,
    private val getUserPlan: GetUserPlan,
    private val paymentManager: PaymentManager,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val currentUser = observeCurrentUser()
        .distinctUntilChanged()

    private val userPlan = currentUser
        .flatMapLatest { user -> getUserPlan(user.userId) }
        .catch {
            snackbarDispatcher(AccountSnackbarMessage.GetUserInfoError)
            throw it
        }
        .distinctUntilChanged()

    val state: StateFlow<AccountUiState> = combine(
        currentUser.asLoadingResult(),
        userPlan.asLoadingResult(),
        ffPreferencesRepository.get<Boolean>(FeatureFlags.IAP_ENABLED)
    ) { userResult, userPlanResult, iapEnabled ->
        val plan = when (userPlanResult) {
            LoadingResult.Loading -> PlanSection.Loading
            is LoadingResult.Error -> {
                PassLogger.e(TAG, userPlanResult.exception, "Error retrieving user plan")
                PlanSection.Hide
            }

            is LoadingResult.Success -> when (val plan = userPlanResult.data) {
                UserPlan.Subuser -> PlanSection.Hide
                UserPlan.Free -> PlanSection.Data(planName = plan.humanReadableName())
                is UserPlan.Paid -> PlanSection.Data(planName = plan.humanReadableName())
            }
        }
        val isUpgradeAvailable = paymentManager.isUpgradeAvailable()
        val isPaid = (userPlanResult as? LoadingResult.Success)?.data is UserPlan.Paid
        when (userResult) {
            LoadingResult.Loading -> AccountUiState.Initial
            is LoadingResult.Error -> AccountUiState(
                email = null,
                plan = PlanSection.Hide,
                isLoadingState = IsLoadingState.NotLoading,
                showUpgradeButton = iapEnabled
            )

            is LoadingResult.Success -> AccountUiState(
                email = userResult.data.email,
                plan = plan,
                isLoadingState = IsLoadingState.NotLoading,
                showUpgradeButton = iapEnabled && isUpgradeAvailable && !isPaid
            )
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AccountUiState.Initial
        )

    companion object {
        private const val TAG = "AccountViewModel"
    }
}
