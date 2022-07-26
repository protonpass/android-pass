package me.proton.android.pass.ui.autofill.save

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassKeyLogger
import me.proton.android.pass.log.e
import me.proton.core.account.domain.entity.Account
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.autofill.service.entities.SecretSaveInfo
import me.proton.core.pass.domain.usecases.AddSecret
import me.proton.core.pass.domain.usecases.GetAddressesForUserId
import me.proton.core.pass.domain.usecases.ObserveAccounts
import me.proton.core.user.domain.entity.UserAddress

@HiltViewModel
class AutofillSaveSecretViewModel @Inject constructor(
    private val addSecret: AddSecret,
    private val getAddressesForUserId: GetAddressesForUserId,
    observeAccounts: ObserveAccounts,
) : ViewModel() {

    private val accountsWithAddresses = observeAccounts()
        .map { accounts ->
            accounts.map { AccountWithAddresses(AccountData(it), getAddressesForUserId(it.userId)) }
        }

    private var mutableState = MutableStateFlow<State>(State.Loading)
    val state = mutableState.asStateFlow()

    init {
        observeCurrentUserAddresses()
    }

    private fun observeCurrentUserAddresses() = viewModelScope.launch {
        accountsWithAddresses.collect { accounts ->
            mutableState.tryEmit(State.Ready(accounts))
        }
    }

    fun save(
        address: UserAddress,
        secretSaveInfo: SecretSaveInfo
    ) = flow<State> {
        addSecret(
            address.userId,
            address.addressId,
            secretSaveInfo.name,
            secretSaveInfo.secretType,
            secretSaveInfo.secretValue,
            secretSaveInfo.appPackageName
        )
        emit(State.Success)
    }.catch {
        PassKeyLogger.e(it)
        emit(State.Failure(null))
    }.onEach {
        mutableState.tryEmit(it)
    }.launchIn(viewModelScope)

    sealed class State {
        object Loading : State()
        data class Ready(val accounts: List<AccountWithAddresses>) : State()
        object Success : State()
        data class Failure(val message: String?) : State()
    }

    data class AccountWithAddresses(
        val account: AccountData,
        val addresses: List<UserAddress>,
    )

    data class AccountData(
        val userId: UserId,
        val username: String,
    ) {
        constructor(account: Account) : this(account.userId, account.username)
    }
}
