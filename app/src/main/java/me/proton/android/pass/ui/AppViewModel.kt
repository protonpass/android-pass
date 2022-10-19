package me.proton.android.pass.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.BuildConfig
import me.proton.android.pass.R
import me.proton.android.pass.log.PassLogger
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.common.api.onError
import me.proton.core.pass.common.api.onSuccess
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.entity.NewVault
import me.proton.core.pass.domain.usecases.CreateVault
import me.proton.core.pass.domain.usecases.GetCurrentShare
import me.proton.core.pass.domain.usecases.GetCurrentUserId
import me.proton.core.pass.domain.usecases.ObserveCurrentUser
import me.proton.core.pass.domain.usecases.RefreshShares
import me.proton.core.pass.presentation.components.navigation.drawer.DrawerUiState
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawerSection
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUser,
    getCurrentUserId: GetCurrentUserId,
    getCurrentShare: GetCurrentShare,
    createVault: CreateVault,
    refreshShares: RefreshShares,
    cryptoContext: CryptoContext
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val currentUserFlow = observeCurrentUser().filterNotNull()
    private val drawerSectionState: MutableStateFlow<NavigationDrawerSection> =
        MutableStateFlow(NavigationDrawerSection.Items)

    val drawerUiState: StateFlow<DrawerUiState> = combine(
        currentUserFlow,
        drawerSectionState
    ) { user, sectionState ->
        DrawerUiState(
            appNameResId = R.string.app_name,
            appVersion = BuildConfig.VERSION_NAME,
            currentUser = user,
            selectedSection = sectionState
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DrawerUiState(
                appNameResId = R.string.app_name,
                appVersion = BuildConfig.VERSION_NAME
            )
        )

    init {
        viewModelScope.launch(coroutineExceptionHandler) {
            val userIdResult: Result<UserId> = getCurrentUserId()
            userIdResult
                .onSuccess { userId ->
                    refreshShares(userId)
                        .onError { onInitError(it, "Refresh shares error") }
                    getCurrentShare(userId)
                        .onSuccess { onShareListReceived(it, userId, createVault, cryptoContext) }
                        .onError { onInitError(it, "Observe shares error") }
                }
                .onError { onInitError(it, "UserId error") }
        }
    }

    private suspend fun onShareListReceived(
        list: List<Share>,
        userId: UserId,
        createVault: CreateVault,
        cryptoContext: CryptoContext
    ) {
        if (list.isEmpty()) {
            val vault = NewVault(
                name = "Personal".encrypt(cryptoContext.keyStoreCrypto),
                description = "Personal vault".encrypt(cryptoContext.keyStoreCrypto)
            )
            createVault(userId, vault)
                .onError { onInitError(it, "Create Vault error") }
        }
    }

    private fun onInitError(throwable: Throwable?, message: String) {
        PassLogger.i(TAG, throwable ?: Exception(message), message)
    }

    fun onDrawerSectionChanged(drawerSection: NavigationDrawerSection) {
        drawerSectionState.update { drawerSection }
    }

    companion object {
        private const val TAG = "AppViewModel"
    }
}
