package proton.android.pass.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import proton.android.pass.R
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.commonuimodels.api.ShareUiModelWithItemCount
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.log.api.PassLogger
import proton.android.pass.network.api.NetworkMonitor
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.presentation.navigation.drawer.DrawerUiState
import proton.android.pass.presentation.navigation.drawer.ItemTypeSection
import proton.android.pass.presentation.navigation.drawer.NavigationDrawerSection
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUser,
    preferenceRepository: UserPreferencesRepository,
    observeVaults: ObserveVaults,
    itemRepository: ItemRepository,
    networkMonitor: NetworkMonitor,
    private val snackbarMessageRepository: SnackbarMessageRepository
) : ViewModel() {

    private val currentUserFlow = observeCurrentUser().filterNotNull()
    private val drawerSectionState: MutableStateFlow<NavigationDrawerSection> =
        MutableStateFlow(NavigationDrawerSection.AllItems())

    private val themePreference: Flow<ThemePreference> = preferenceRepository
        .getThemePreference()
        .asResultWithoutLoading()
        .map { getThemePreference(it) }


    data class ShareUiModelsWithTrashedCount(
        val models: List<ShareUiModelWithItemCount>,
        val trashedCount: Long
    )

    private val allShareUiModelFlow: Flow<ShareUiModelsWithTrashedCount> = observeVaults()
        .map { shares ->
            when (shares) {
                LoadingResult.Loading -> ShareUiModelsWithTrashedCount(emptyList(), 0)
                is LoadingResult.Error -> {
                    PassLogger.e(TAG, shares.exception, "Cannot retrieve all shares")
                    ShareUiModelsWithTrashedCount(emptyList(), 0)
                }
                is LoadingResult.Success -> {
                    var totalTrashed = 0L
                    val res = shares.data
                        .map {
                            totalTrashed += it.trashedItemCount
                            ShareUiModelWithItemCount(
                                id = it.shareId,
                                name = it.name,
                                activeItemCount = it.activeItemCount,
                                trashedItemCount = it.trashedItemCount
                            )
                        }

                    ShareUiModelsWithTrashedCount(
                        models = res,
                        trashedCount = totalTrashed
                    )
                }
            }
        }
        .distinctUntilChanged()

    private val itemCountSummaryFlow = combine(
        currentUserFlow,
        drawerSectionState,
        allShareUiModelFlow
    ) { user, drawerSection, allShares ->
        val shares: List<ShareUiModelWithItemCount> = when (drawerSection) {
            is ItemTypeSection ->
                drawerSection.shareId
                    ?.let { selectedShare ->
                        allShares.models.filter { share -> share.id == selectedShare }
                    }
                    ?: allShares.models
            else -> allShares.models
        }
        user to shares
    }
        .flatMapLatest { pair ->
            itemRepository.observeItemCountSummary(pair.first.userId, pair.second.map { it.id })
        }

    private val drawerStateFlow: Flow<DrawerState> = combine(
        drawerSectionState,
        itemCountSummaryFlow,
        allShareUiModelFlow
    ) { drawerSection, itemCountSummary, shares ->
        DrawerState(
            section = drawerSection,
            itemCountSummary = itemCountSummary,
            shares = shares.models,
            totalTrashedItems = shares.trashedCount
        )
    }

    private data class DrawerState(
        val section: NavigationDrawerSection,
        val itemCountSummary: ItemCountSummary,
        val shares: List<ShareUiModelWithItemCount>,
        val totalTrashedItems: Long
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
                itemCountSummary = drawerState.itemCountSummary,
                shares = drawerState.shares,
                trashedItemCount = drawerState.totalTrashedItems
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

    fun onDrawerSectionChanged(drawerSection: NavigationDrawerSection) {
        drawerSectionState.update { drawerSection }
    }

    fun onSnackbarMessageDelivered() {
        viewModelScope.launch {
            snackbarMessageRepository.snackbarMessageDelivered()
        }
    }

    private fun getThemePreference(state: LoadingResult<ThemePreference>): ThemePreference =
        when (state) {
            LoadingResult.Loading -> ThemePreference.System
            is LoadingResult.Success -> state.data
            is LoadingResult.Error -> {
                PassLogger.w(TAG, state.exception, "Error getting ThemePreference")
                ThemePreference.System
            }
        }

    companion object {
        private const val TAG = "AppViewModel"
    }
}
