package me.proton.android.pass.ui.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import me.proton.android.pass.BuildConfig
import me.proton.android.pass.R
import me.proton.android.pass.ui.user.UserViewModel
import me.proton.core.domain.arch.mapSuccessValueOrNull
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawerViewEvent
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawerViewState
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import javax.inject.Inject

@ExperimentalMaterialApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    userManager: UserManager,
    savedStateHandle: SavedStateHandle,
): ViewModel(), UserViewModel by UserViewModel(savedStateHandle) {

    val initialViewState = getViewState(user = null)
    val viewState: Flow<ViewState> = userManager.getUserFlow(SessionUserId(userId.id))
        .mapSuccessValueOrNull()
        .distinctUntilChanged()
        .map(::getViewState)

    private fun getViewState(user: User?) =
        ViewState(
            navigationDrawerViewState = NavigationDrawerViewState(
                R.string.app_name,
                BuildConfig.VERSION_NAME,
                currentUser = user
            )
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

    @Immutable
    data class ViewState(
        val navigationDrawerViewState: NavigationDrawerViewState
    )

    interface ViewEvent {
        val navigationDrawerViewEvent: NavigationDrawerViewEvent
    }
}