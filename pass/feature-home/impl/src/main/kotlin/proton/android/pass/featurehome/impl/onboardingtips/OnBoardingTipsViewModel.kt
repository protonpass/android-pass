package proton.android.pass.featurehome.impl.onboardingtips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.AUTOFILL
import proton.android.pass.preferences.HasDismissedAutofillBanner
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class OnBoardingTipsViewModel @Inject constructor(
    private val autofillManager: AutofillManager,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val shouldShowAutofillFlow: Flow<Boolean> = combine(
        autofillManager.getAutofillStatus(),
        preferencesRepository.getHasDismissedAutofillBanner(),
        ::shouldShowBanner
    )

    val state: StateFlow<OnBoardingTipsUiState> = shouldShowAutofillFlow
        .map {
            if (it) {
                OnBoardingTipsUiState(persistentSetOf(AUTOFILL))
            } else {
                OnBoardingTipsUiState(persistentSetOf())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = runBlocking {
                val shouldShowAutofill = shouldShowAutofillFlow.first()
                if (shouldShowAutofill) {
                    OnBoardingTipsUiState(persistentSetOf(AUTOFILL))
                } else {
                    OnBoardingTipsUiState(persistentSetOf())
                }
            }
        )

    private fun shouldShowBanner(
        autofillSupportedStatus: AutofillSupportedStatus,
        hasDismissedAutofillBanner: HasDismissedAutofillBanner
    ): Boolean =
        autofillSupportedStatus is AutofillSupportedStatus.Supported &&
            autofillSupportedStatus.status !is AutofillStatus.EnabledByOurService &&
            hasDismissedAutofillBanner is HasDismissedAutofillBanner.NotDismissed

    fun onClick(onBoardingTipPage: OnBoardingTipPage) {
        when (onBoardingTipPage) {
            AUTOFILL -> autofillManager.openAutofillSelector()
        }
    }

    fun onDismiss(onBoardingTipPage: OnBoardingTipPage) {
        when (onBoardingTipPage) {
            AUTOFILL -> viewModelScope.launch {
                preferencesRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.Dismissed)
            }
        }
    }
}
