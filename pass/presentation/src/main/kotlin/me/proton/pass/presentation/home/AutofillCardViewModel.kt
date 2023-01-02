package me.proton.pass.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.proton.android.pass.autofill.api.AutofillManager
import me.proton.android.pass.autofill.api.AutofillStatus
import me.proton.android.pass.autofill.api.AutofillSupportedStatus
import me.proton.android.pass.preferences.HasDismissedAutofillBanner
import me.proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AutofillCardViewModel @Inject constructor(
    private val autofillManager: AutofillManager,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val state: StateFlow<Boolean> = combine(
        autofillManager.getAutofillStatus(),
        preferencesRepository.getHasDismissedAutofillBanner(),
        ::shouldShowBanner
    )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = runBlocking {
                val autofillSupportedStatus = autofillManager.getAutofillStatus()
                    .firstOrNull()
                    ?: return@runBlocking false
                val hasDismissedAutofillBanner =
                    preferencesRepository.getHasDismissedAutofillBanner()
                        .firstOrNull()
                        ?: return@runBlocking false
                shouldShowBanner(autofillSupportedStatus, hasDismissedAutofillBanner)
            }
        )

    private fun shouldShowBanner(
        autofillSupportedStatus: AutofillSupportedStatus,
        hasDismissedAutofillBanner: HasDismissedAutofillBanner
    ): Boolean =
        autofillSupportedStatus is AutofillSupportedStatus.Supported &&
            autofillSupportedStatus.status !is AutofillStatus.EnabledByOurService &&
            hasDismissedAutofillBanner is HasDismissedAutofillBanner.NotDismissed

    fun onClick() {
        autofillManager.openAutofillSelector()
    }

    fun onDismiss() {
        viewModelScope.launch {
            preferencesRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.Dismissed)
        }
    }
}
