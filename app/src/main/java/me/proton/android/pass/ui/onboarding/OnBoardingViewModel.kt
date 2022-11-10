package me.proton.android.pass.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.preferences.HasCompletedOnBoarding
import me.proton.android.pass.preferences.PreferenceRepository
import me.proton.pass.common.api.asResultWithoutLoading
import me.proton.pass.common.api.onError
import me.proton.pass.common.api.onSuccess
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _onboardingUiState = MutableStateFlow(false)
    val onboardingUiState: StateFlow<Boolean> = _onboardingUiState

    fun onBoardingComplete() = viewModelScope.launch {
        preferenceRepository.setHasCompletedOnBoarding(HasCompletedOnBoarding.Completed)
            .asResultWithoutLoading()
            .collect { result ->
                result
                    .onSuccess { _onboardingUiState.tryEmit(true) }
                    .onError {
                        val message = "Could not save HasCompletedOnBoarding preference"
                        PassLogger.e(TAG, it ?: RuntimeException(message))
                    }
            }
    }

    companion object {
        private const val TAG = "OnBoardingViewModel"
    }
}
