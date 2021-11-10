package me.proton.android.pass.ui.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.proton.android.pass.BuildConfig
import me.proton.android.pass.R
import me.proton.android.pass.ui.user.UserViewModel
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.arch.mapSuccessValueOrNull
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.pass.common_secret.Secret
import me.proton.core.pass.domain.usecases.DeleteSecret
import me.proton.core.pass.domain.usecases.ObserveSecrets
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawerViewEvent
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawerViewState
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import javax.inject.Inject

@ExperimentalMaterialApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    accountManager: AccountManager,
    userManager: UserManager,
    savedStateHandle: SavedStateHandle,
    private val observeSecrets: ObserveSecrets,
    private val deleteSecret: DeleteSecret,
): ViewModel(), UserViewModel by UserViewModel(savedStateHandle) {

    val initialViewState = getViewState(user = null, secrets = emptyList())
    private val getCurrentUserIdFlow = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userManager.getUserFlow(it) }
        .mapSuccessValueOrNull()
        .distinctUntilChanged()
    private val listSecretsForUserId = getCurrentUserIdFlow
        .filterNotNull()
        .flatMapLatest { observeSecrets(it.userId) }
        .distinctUntilChanged()

    val viewState: Flow<ViewState> = combine(
            getCurrentUserIdFlow,
            listSecretsForUserId,
            ::getViewState
        )

    private fun getViewState(user: User?, secrets: List<Secret>) =
        ViewState(
            navigationDrawerViewState = NavigationDrawerViewState(
                R.string.app_name,
                BuildConfig.VERSION_NAME,
                currentUser = user
            ),
            secrets,
        )

    fun viewEvent(
        navigateToSigningOut: () -> Unit,
    ): ViewEvent = object : ViewEvent {
        override val navigationDrawerViewEvent: NavigationDrawerViewEvent =
            object : NavigationDrawerViewEvent {
                override val onSettings = {}
                override val onSignOut = navigateToSigningOut
                override val onBugReport = {}
            }
    }

    fun deleteSecret(secret: Secret) = viewModelScope.launch {
        secret.id?.let { deleteSecret(it) }
    }

    @Immutable
    data class ViewState(
        val navigationDrawerViewState: NavigationDrawerViewState,
        val secrets: List<Secret>
    )

    interface ViewEvent {
        val navigationDrawerViewEvent: NavigationDrawerViewEvent
    }
}
