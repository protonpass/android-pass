package proton.android.pass.featurehome.impl

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.biometry.NeedsBiometricAuth
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
    preferenceRepository: UserPreferencesRepository,
    needsBiometricAuth: NeedsBiometricAuth
) : ViewModel() {

    val navHomeUiState: SharedFlow<NavHomeUiState> = combine(
        needsBiometricAuth().distinctUntilChanged(),
        preferenceRepository.getHasCompletedOnBoarding().asResultWithoutLoading()
    ) { shouldAuthenticate, hasCompletedOnBoarding ->
        NavHomeUiState(
            shouldAuthenticate = shouldAuthenticate.some(),
            hasCompletedOnBoarding = when (hasCompletedOnBoarding) {
                is LoadingResult.Success -> hasCompletedOnBoarding.data.value().some()
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
    val shouldAuthenticate: Option<Boolean>,
    val hasCompletedOnBoarding: Option<Boolean>
) {
    companion object {
        val Initial = NavHomeUiState(None, None)
    }
}
