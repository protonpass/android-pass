package me.proton.android.pass.ui.autofill.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.android.pass.log.PassKeyLogger
import me.proton.android.pass.log.e
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.commonsecret.Secret
import me.proton.core.pass.domain.usecases.GetAddressById
import me.proton.core.pass.domain.usecases.SearchSecretWithUri
import me.proton.core.user.domain.entity.AddressId

@HiltViewModel
class AutofillSearchSecretViewModel @Inject constructor(
    private val searchSecretWithUri: SearchSecretWithUri,
    private val getAddressById: GetAddressById
) : ViewModel() {

    private val mutableState = MutableStateFlow<State>(State.Searching)
    val state = mutableState.asStateFlow()

    fun searchByPackageName(packageName: String) = flow {
        emit(State.Searching)
        val results = searchSecretWithUri(packageName)
        val ids = results.groupBy { it.userId to it.addressId }.keys
        val addresses = ids.mapNotNull {
            getAddressById(UserId(it.first), AddressId(it.second))
        }.groupBy { it.addressId }
        val mappedResults = results.mapNotNull { secret ->
            val address = addresses[AddressId(secret.addressId)]?.firstOrNull()
            address?.let { ListSecretItem(secret, it.email) }
        }
        emit(State.Ready(mappedResults))
    }.catch {
        PassKeyLogger.e(it)
        emit(State.Idle)
    }.onEach {
        mutableState.tryEmit(it)
    }.launchIn(viewModelScope)

    sealed class State {
        object Idle : State()
        object Searching : State()
        data class Ready(val results: List<ListSecretItem>) : State()
    }
}

data class ListSecretItem(
    val secret: Secret,
    val address: String
)
