package me.proton.android.pass.ui.launcher

import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import me.proton.core.account.domain.entity.*
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.accountmanager.presentation.*
import me.proton.core.auth.presentation.AuthOrchestrator
import me.proton.core.auth.presentation.onAddAccountResult
import me.proton.core.domain.entity.Product
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.humanverification.presentation.HumanVerificationOrchestrator
import me.proton.core.humanverification.presentation.observe
import me.proton.core.humanverification.presentation.onHumanVerificationNeeded
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val authOrchestrator: AuthOrchestrator,
    private val humanVerificationManager: HumanVerificationManager,
    private val humanVerificationOrchestrator: HumanVerificationOrchestrator
): ViewModel() {

    private val _state = MutableStateFlow<State>(State.Processing)
    private val _primaryAccount = MutableStateFlow<Account?>(null)

    private fun onAccountReady() {
        _state.value = State.AccountReady
    }

    private fun onPrimaryNeeded() {
        _state.value = State.PrimaryNeeded
    }

    private fun onStepNeeded() {
        _state.value = State.Processing
    }

    val state = _state.asStateFlow()
    val primaryAccount: StateFlow<Account?> = _primaryAccount.asStateFlow()

    override fun onCleared() {
        authOrchestrator.unregister()
        humanVerificationOrchestrator.unregister()
    }

    fun initialize(context: ComponentActivity) {
        // Account state handling.
        with(authOrchestrator) {
            register(context)
            onAddAccountResult { result ->
                if (result == null) _state.value = State.ExitApp
            }
            accountManager.observe(context.lifecycle, minActiveState = Lifecycle.State.CREATED)
                .onSessionSecondFactorNeeded { startSecondFactorWorkflow(it) }
                .onAccountTwoPassModeNeeded { startTwoPassModeWorkflow(it) }
                .onAccountCreateAddressNeeded { startChooseAddressWorkflow(it) }
                .onAccountTwoPassModeFailed { accountManager.disableAccount(it.userId) }
                .onAccountCreateAddressFailed { accountManager.disableAccount(it.userId) }
                .onAccountDisabled { accountManager.removeAccount(it.userId) }
                .disableInitialNotReadyAccounts()
        }

        // HumanVerification State handling.
        with(humanVerificationOrchestrator) {
            register(context)
            humanVerificationManager
                .observe(context.lifecycle, minActiveState = Lifecycle.State.RESUMED)
                .onHumanVerificationNeeded { startHumanVerificationWorkflow(it) }
        }

        // Check if we already have Ready account.
        accountManager.getAccounts()
            .flowWithLifecycle(context.lifecycle, Lifecycle.State.CREATED)
            .onEach { accounts ->
                when {
                    accounts.isEmpty() || accounts.all { it.isDisabled() } -> onPrimaryNeeded()
                    accounts.any { it.isReady() } -> onAccountReady()
                    accounts.any { it.isStepNeeded() } -> onStepNeeded()
                }
            }.launchIn(context.lifecycleScope)

        accountManager.getPrimaryAccount()
            .flowWithLifecycle(context.lifecycle, Lifecycle.State.CREATED)
            .onEach { account -> _primaryAccount.value = account }
            .launchIn(context.lifecycleScope)
    }

    fun addAccount() {
        authOrchestrator.startAddAccountWorkflow(AccountType.Internal, Product.Drive)
    }

    sealed class State {
        object PrimaryNeeded : State()
        object AccountReady : State()
        object Processing : State()
        object ExitApp : State()
    }
}