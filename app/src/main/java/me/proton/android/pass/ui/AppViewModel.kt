package me.proton.android.pass.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.BuildConfig
import me.proton.android.pass.R
import me.proton.android.pass.data.api.usecases.CreateVault
import me.proton.android.pass.data.api.usecases.GetCurrentShare
import me.proton.android.pass.data.api.usecases.GetCurrentUserId
import me.proton.android.pass.data.api.usecases.ObserveCurrentUser
import me.proton.android.pass.data.api.usecases.RefreshShares
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.android.pass.preferences.PreferenceRepository
import me.proton.android.pass.preferences.ThemePreference
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.asResultWithoutLoading
import me.proton.pass.common.api.onError
import me.proton.pass.common.api.onSuccess
import me.proton.pass.domain.Share
import me.proton.pass.domain.entity.NewVault
import me.proton.pass.presentation.components.navigation.drawer.DrawerUiState
import me.proton.pass.presentation.components.navigation.drawer.NavigationDrawerSection
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUser,
    preferenceRepository: PreferenceRepository,
    private val getCurrentUserId: GetCurrentUserId,
    private val getCurrentShare: GetCurrentShare,
    private val createVault: CreateVault,
    private val refreshShares: RefreshShares,
    private val cryptoContext: CryptoContext,
    private val snackbarMessageRepository: SnackbarMessageRepository
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val currentUserFlow = observeCurrentUser().filterNotNull()
    private val drawerSectionState: MutableStateFlow<NavigationDrawerSection> =
        MutableStateFlow(NavigationDrawerSection.Items)

    private val themePreference: Flow<ThemePreference> = preferenceRepository
        .getThemePreference()
        .asResultWithoutLoading()
        .map { getThemePreference(it) }

    val appUiState: StateFlow<AppUiState> = combine(
        currentUserFlow,
        drawerSectionState,
        snackbarMessageRepository.snackbarMessage,
        themePreference
    ) { user, sectionState, snackbarMessage, theme ->
        AppUiState(
            snackbarMessage = snackbarMessage,
            drawerUiState = DrawerUiState(
                appNameResId = R.string.app_name,
                appVersion = BuildConfig.VERSION_NAME,
                currentUser = user,
                selectedSection = sectionState,
                internalDrawerEnabled = BuildConfig.FLAVOR == "dev"
            ),
            theme = theme
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppUiState.Initial
        )

    fun onStart() = viewModelScope.launch(coroutineExceptionHandler) {
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


    private suspend fun onShareListReceived(
        list: List<Share>,
        userId: UserId,
        createVault: me.proton.android.pass.data.api.usecases.CreateVault,
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

    fun onSnackbarMessageDelivered() {
        viewModelScope.launch {
            snackbarMessageRepository.snackbarMessageDelivered()
        }
    }

    private fun getThemePreference(state: Result<ThemePreference>): ThemePreference =
        when (state) {
            Result.Loading -> ThemePreference.System
            is Result.Success -> state.data
            is Result.Error -> {
                val message = "Error getting ThemePreference"
                PassLogger.e(TAG, state.exception ?: Exception(message))
                ThemePreference.System
            }
        }

    companion object {
        private const val TAG = "AppViewModel"
    }
}
