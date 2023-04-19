package proton.android.pass.featureaccount.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.proton.core.user.domain.entity.User
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.UserPlan
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUser,
    private val getUserPlan: GetUserPlan,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    val state = observeCurrentUser()
        .asLoadingResult()
        .flatMapLatest { userResult ->
            when (userResult) {
                LoadingResult.Loading -> flowOf(AccountUiState.Initial)
                is LoadingResult.Error -> flowOf(
                    AccountUiState(
                        email = null,
                        plan = PlanSection.Hide,
                        isLoadingState = IsLoadingState.NotLoading,
                    )
                )

                is LoadingResult.Success -> onUser(userResult.data)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AccountUiState.Initial
        )

    private fun onUser(user: User): Flow<AccountUiState> =
        getUserPlan(user.userId)
            .asLoadingResult()
            .map {
                val plan = when (it) {
                    LoadingResult.Loading -> PlanSection.Loading
                    is LoadingResult.Error -> {
                        PassLogger.e(TAG, it.exception, "Error retrieving user plan")
                        snackbarDispatcher(AccountSnackbarMessage.GetUserInfoError)
                        PlanSection.Hide
                    }

                    is LoadingResult.Success -> when (val plan = it.data) {
                        UserPlan.Subuser -> PlanSection.Hide
                        UserPlan.Free -> PlanSection.Data(planName = plan.humanReadableName())
                        is UserPlan.Paid -> PlanSection.Data(planName = plan.humanReadableName())
                    }
                }

                AccountUiState(
                    email = user.email,
                    plan = plan,
                    isLoadingState = IsLoadingState.NotLoading
                )
            }


    companion object {
        private const val TAG = "AccountViewModel"
    }
}
