package proton.android.pass.featureaccount.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUser,
    getUserPlan: GetUserPlan,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    val state = observeCurrentUser()
        .asLoadingResult()
        .map { userResult ->
            when (userResult) {
                is LoadingResult.Error -> AccountUiState(isLoadingState = IsLoadingState.NotLoading)
                LoadingResult.Loading -> AccountUiState()
                is LoadingResult.Success -> {
                    val user = userResult.data

                    val plan = runCatching {
                        getUserPlan(user.userId)
                    }.fold(
                        onSuccess = { plan -> plan.humanReadable },
                        onFailure = {
                            PassLogger.e(TAG, it, "Error getting user plan")
                            snackbarDispatcher(AccountSnackbarMessage.GetUserInfoError)
                            null
                        }
                    )

                    AccountUiState(
                        email = user.email,
                        plan = plan,
                        isLoadingState = IsLoadingState.NotLoading
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AccountUiState()
        )

    companion object {
        private const val TAG = "AccountViewModel"
    }
}

data class AccountUiState(
    val email: String? = null,
    val plan: String? = null,
    val isLoadingState: IsLoadingState = IsLoadingState.Loading
)
