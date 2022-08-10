package me.proton.android.pass.ui.help

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.proton.android.pass.BuildConfig
import me.proton.android.pass.R
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawerViewState
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User

@HiltViewModel
class HelpViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val userManager: UserManager,
) : ViewModel() {

    val initialViewState = getViewState(
        user = null,
    )

    private val getCurrentUserIdFlow = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userManager.observeUser(it) }
        .distinctUntilChanged()

    val state = getCurrentUserIdFlow.map { getViewState(it) }

    private fun getViewState(
        user: User?,
    ): ViewState =
        ViewState(
            navigationDrawerViewState = NavigationDrawerViewState(
                R.string.app_name,
                BuildConfig.VERSION_NAME,
                currentUser = user
            ),
        )

    @Immutable
    data class ViewState(
        val navigationDrawerViewState: NavigationDrawerViewState,
    )
}
