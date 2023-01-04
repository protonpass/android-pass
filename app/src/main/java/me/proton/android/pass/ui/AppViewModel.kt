package me.proton.android.pass.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.proton.android.pass.R
import me.proton.android.pass.data.api.ItemCountSummary
import me.proton.android.pass.data.api.repositories.ItemRepository
import me.proton.android.pass.data.api.usecases.ApplyPendingEvents
import me.proton.android.pass.data.api.usecases.CreateVault
import me.proton.android.pass.data.api.usecases.GetCurrentShare
import me.proton.android.pass.data.api.usecases.GetCurrentUserId
import me.proton.android.pass.data.api.usecases.ObserveActiveShare
import me.proton.android.pass.data.api.usecases.ObserveCurrentUser
import me.proton.android.pass.data.api.usecases.RefreshShares
import me.proton.android.pass.log.api.PassLogger
import me.proton.android.pass.network.api.NetworkMonitor
import me.proton.android.pass.network.api.NetworkStatus
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.android.pass.preferences.ThemePreference
import me.proton.android.pass.preferences.UserPreferencesRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.asResultWithoutLoading
import me.proton.pass.common.api.onError
import me.proton.pass.common.api.onSuccess
import me.proton.pass.domain.Share
import me.proton.pass.domain.entity.NewVault
import me.proton.pass.presentation.navigation.drawer.DrawerUiState
import me.proton.pass.presentation.navigation.drawer.NavigationDrawerSection
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUser,
    preferenceRepository: UserPreferencesRepository,
    observeActiveShare: ObserveActiveShare,
    itemRepository: ItemRepository,
    networkMonitor: NetworkMonitor,
    private val getCurrentUserId: GetCurrentUserId,
    private val getCurrentShare: GetCurrentShare,
    private val createVault: CreateVault,
    private val refreshShares: RefreshShares,
    private val cryptoContext: CryptoContext,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val applyPendingEvents: ApplyPendingEvents
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

    private val itemCountSummaryFlow: Flow<ItemCountSummary> = combine(
        currentUserFlow,
        observeActiveShare()
    ) { user, share -> user.userId to share }
        .flatMapLatest {
            val (userId, shareResult) = it
            when (shareResult) {
                Result.Loading -> flowOf(ItemCountSummary.Initial)
                is Result.Error -> {
                    val message = "Cannot retrieve ItemCountSummary"
                    PassLogger.e(TAG, shareResult.exception ?: Exception(message), message)
                    flowOf(ItemCountSummary.Initial)
                }
                is Result.Success -> {
                    val shareId = shareResult.data
                    if (shareId != null) {
                        itemRepository.observeItemCountSummary(userId, shareId)
                    } else {
                        flowOf(ItemCountSummary.Initial)
                    }
                }
            }
        }

    private val drawerStateFlow: Flow<DrawerState> = combine(
        drawerSectionState,
        itemCountSummaryFlow
    ) { drawerSection, itemCountSummary ->
        DrawerState(
            section = drawerSection,
            itemCountSummary = itemCountSummary
        )
    }

    private data class DrawerState(
        val section: NavigationDrawerSection,
        val itemCountSummary: ItemCountSummary
    )

    private val networkStatus: Flow<NetworkStatus> = networkMonitor
        .connectivity
        .distinctUntilChanged()

    val appUiState: StateFlow<AppUiState> = combine(
        currentUserFlow,
        drawerStateFlow,
        snackbarMessageRepository.snackbarMessage,
        themePreference,
        networkStatus
    ) { user, drawerState, snackbarMessage, theme, network ->
        AppUiState(
            snackbarMessage = snackbarMessage,
            drawerUiState = DrawerUiState(
                appNameResId = R.string.app_name,
                currentUser = user,
                selectedSection = drawerState.section,
                itemCountSummary = drawerState.itemCountSummary
            ),
            theme = theme,
            networkStatus = network
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppUiState.Initial(
                runBlocking {
                    preferenceRepository.getThemePreference().first()
                }
            )
        )

    fun onStart() = viewModelScope.launch(coroutineExceptionHandler) {
        val userIdResult: Result<UserId> = getCurrentUserId()
        userIdResult
            .onSuccess { userId ->
                refreshShares(userId)
                    .onError { onInitError(it, "Refresh shares error") }
                getCurrentShare(userId)
                    .onSuccess { onShareListReceived(it, userId) }
                    .onError { onInitError(it, "Observe shares error") }
            }
            .onError { onInitError(it, "UserId error") }
    }


    private suspend fun onShareListReceived(
        list: List<Share>,
        userId: UserId
    ) {
        if (list.isEmpty()) {
            val vault = NewVault(
                name = "Personal".encrypt(cryptoContext.keyStoreCrypto),
                description = "Personal vault".encrypt(cryptoContext.keyStoreCrypto)
            )
            createVault(userId, vault)
                .onError { onInitError(it, "Create Vault error") }
        } else {
            applyEvents(list, userId)
        }
    }

    private fun applyEvents(list: List<Share>, userId: UserId) = viewModelScope.launch {
        val results = list.map { share ->
            async {
                val res = kotlin.runCatching {
                    applyPendingEvents(userId, share.id)
                }.onFailure {
                    onInitError(it, "Error refreshing share [share_id=${share.id}]")
                }

                res.isSuccess
            }
        }.awaitAll()
        val anyError = results.any { !it }
        if (anyError) {
            snackbarMessageRepository.emitSnackbarMessage(AppSnackbarMessage.CouldNotRefreshItems)
        }
    }

    private suspend fun onInitError(throwable: Throwable?, message: String) {
        PassLogger.e(TAG, throwable ?: Exception(message), message)
        snackbarMessageRepository.emitSnackbarMessage(AppSnackbarMessage.ErrorDuringStartup)
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
