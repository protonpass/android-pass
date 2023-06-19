package proton.android.pass.autofill.ui.autosave

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import proton.android.pass.account.api.AccountOrchestrators
import proton.android.pass.account.api.Orchestrator
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AutosaveActivityViewModel @Inject constructor(
    private val accountOrchestrators: AccountOrchestrators,
    private val preferenceRepository: UserPreferencesRepository
) : ViewModel() {

    fun register(context: ComponentActivity) {
        accountOrchestrators.register(context, listOf(Orchestrator.PlansOrchestrator))
    }

    fun upgrade() = viewModelScope.launch {
        accountOrchestrators.start(Orchestrator.PlansOrchestrator)
    }

    fun onStop() = viewModelScope.launch {
        runBlocking {
            preferenceRepository.setHasAuthenticated(HasAuthenticated.NotAuthenticated)
        }
    }
}
