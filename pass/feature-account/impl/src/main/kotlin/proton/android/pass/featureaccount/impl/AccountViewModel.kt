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
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUser,
    getUserPlan: GetUserPlan
) : ViewModel() {

    val state = observeCurrentUser()
        .asLoadingResult()
        .map {
            when (it) {
                is LoadingResult.Error -> AccountUiState(isLoadingState = IsLoadingState.NotLoading)
                LoadingResult.Loading -> AccountUiState()
                is LoadingResult.Success -> {
                    val plan = getUserPlan(it.data.userId)
                    AccountUiState(
                        it.data.email ?: "",
                        plan.humanReadable,
                        IsLoadingState.NotLoading
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AccountUiState()
        )
}

data class AccountUiState(
    val email: String? = null,
    val plan: String? = null,
    val isLoadingState: IsLoadingState = IsLoadingState.Loading
)
