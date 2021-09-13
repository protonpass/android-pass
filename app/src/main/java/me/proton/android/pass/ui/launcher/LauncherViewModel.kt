package me.proton.android.pass.ui.launcher

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
    private val accountManager: AccountManager,
): ViewModel() {

    var currentViewState = ViewState()

    fun viewState() = accountManager.getPrimaryAccount()
        .transform { account ->
            currentViewState = account?.userId?.let { userId ->
                ViewState(PrimaryAccountState.SignedIn(userId))
            } ?: ViewState(PrimaryAccountState.SigningIn)
            emit(currentViewState)
        }
        .distinctUntilChanged()

    @Immutable
    data class ViewState(
        val primaryAccountState: PrimaryAccountState = PrimaryAccountState.SignedOut
    )
}

sealed class PrimaryAccountState {
    object SigningOut : PrimaryAccountState()
    object SignedOut: PrimaryAccountState()
    object SigningIn: PrimaryAccountState()
    data class SignedIn(val userId: UserId) : PrimaryAccountState()
}
