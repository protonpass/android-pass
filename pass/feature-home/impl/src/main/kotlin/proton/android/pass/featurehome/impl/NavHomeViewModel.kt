package proton.android.pass.featurehome.impl

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.common.api.some
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

@HiltViewModel
class NavHomeViewModel @Inject constructor(
    preferenceRepository: UserPreferencesRepository
) : ViewModel() {

    val navHomeUiState: StateFlow<NavHomeUiState> = preferenceRepository.getHasCompletedOnBoarding()
        .asResultWithoutLoading()
        .map {
            NavHomeUiState(
                hasCompletedOnBoarding = when (it) {
                    is LoadingResult.Success -> it.data.value().some()
                    else -> None
                }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NavHomeUiState.Initial
        )
}

@Immutable
data class NavHomeUiState(
    val hasCompletedOnBoarding: Option<Boolean>
) {
    companion object {
        val Initial = NavHomeUiState(None)
    }
}
