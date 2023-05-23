package proton.android.pass.autofill.ui.autofill.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.autofill.AutofillDisplayed
import proton.android.pass.autofill.AutofillTriggerSource
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.extensions.toAutoFillItem
import proton.android.pass.autofill.service.R
import proton.android.pass.autofill.ui.autofill.ItemFieldMapper
import proton.android.pass.autofill.ui.autofill.select.SelectItemSnackbarMessage.LoadItemsError
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.map
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonui.api.ItemSorter.sortByCreationAsc
import proton.android.pass.commonui.api.ItemSorter.sortByCreationDesc
import proton.android.pass.commonui.api.ItemSorter.sortByMostRecent
import proton.android.pass.commonui.api.ItemSorter.sortByTitleAsc
import proton.android.pass.commonui.api.ItemSorter.sortByTitleDesc
import proton.android.pass.commonui.api.ItemSorter.sortSuggestionsByMostRecent
import proton.android.pass.commonui.api.ItemUiFilter
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateAutofillItemData
import proton.android.pass.featuresearchoptions.api.SearchOptionsRepository
import proton.android.pass.featuresearchoptions.api.SearchSortingType
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.GetTotpCodeFromUri
import proton.pass.domain.ItemId
import proton.pass.domain.PlanType
import proton.pass.domain.ShareId
import proton.pass.domain.ShareSelection
import java.net.URI
import javax.inject.Inject

@HiltViewModel
class SelectItemViewModel @Inject constructor(
    private val updateAutofillItem: UpdateAutofillItem,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val clipboardManager: ClipboardManager,
    private val getTotpCodeFromUri: GetTotpCodeFromUri,
    private val toastManager: ToastManager,
    private val preferenceRepository: UserPreferencesRepository,
    observeActiveItems: ObserveActiveItems,
    getSuggestedLoginItems: GetSuggestedLoginItems,
    telemetryManager: TelemetryManager,
    observeVaults: ObserveVaults,
    searchOptionsRepository: SearchOptionsRepository,
    getUserPlan: GetUserPlan,
    observeUpgradeInfo: ObserveUpgradeInfo,
    clock: Clock,
) : ViewModel() {

    init {
        telemetryManager.sendEvent(AutofillDisplayed(AutofillTriggerSource.App))
    }

    private val autofillAppState: MutableStateFlow<Option<AutofillAppState>> =
        MutableStateFlow(None)

    private val searchQueryState: MutableStateFlow<String> = MutableStateFlow("")
    private val isInSearchModeState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isProcessingSearchState: MutableStateFlow<IsProcessingSearchState> =
        MutableStateFlow(IsProcessingSearchState.NotLoading)
    private val shouldScrollToTopFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val sortingSelectionFlow = searchOptionsRepository.observeSortingOption()
        .distinctUntilChanged()
        .onEach { shouldScrollToTopFlow.update { true } }

    private val searchWrapper = combine(
        searchQueryState,
        isInSearchModeState,
        isProcessingSearchState,
        ::SearchWrapper
    )

    private data class SearchWrapper(
        val searchQuery: String,
        val isInSearchMode: Boolean,
        val isProcessingSearch: IsProcessingSearchState
    )

    private val planTypeFlow: Flow<PlanType> = getUserPlan()
        .map { it.planType }
        .distinctUntilChanged()

    private val vaultsFlow = observeVaults().asLoadingResult()

    private val shareIdToSharesFlow = vaultsFlow
        .map { vaultsResult ->
            val vaults = vaultsResult.getOrNull() ?: emptyList()
            vaults.associate { it.shareId to ShareUiModel.fromVault(it) }
                .toPersistentMap()
        }
        .distinctUntilChanged()

    private val itemUiModelFlow: Flow<LoadingResult<List<ItemUiModel>>> = combine(
        planTypeFlow,
        vaultsFlow
    ) { plan, vaults -> plan to vaults }
        .flatMapLatest { planAndVaults ->
            val (planType, vaultsRes) = planAndVaults

            val vaults = when (vaultsRes) {
                is LoadingResult.Success -> vaultsRes.data
                is LoadingResult.Error -> {
                    PassLogger.w(TAG, vaultsRes.exception, "Error observing vaults")
                    return@flatMapLatest flowOf(LoadingResult.Error(vaultsRes.exception))
                }

                LoadingResult.Loading -> return@flatMapLatest flowOf(LoadingResult.Loading)
            }

            val selection = when (planType) {
                is PlanType.Paid, is PlanType.Trial -> ShareSelection.AllShares
                else -> {
                    val primaryVault = vaults.firstOrNull { it.isPrimary }
                        ?: vaults.firstOrNull()
                        ?: return@flatMapLatest flowOf(LoadingResult.Error(IllegalStateException("No vaults found")))
                    ShareSelection.Share(primaryVault.shareId)
                }
            }

            observeActiveItems(
                filter = ItemTypeFilter.Logins,
                shareSelection = selection
            ).asResultWithoutLoading()

        }
        .map { itemResult ->
            itemResult.map { list ->
                encryptionContextProvider.withEncryptionContext {
                    list.map { it.toUiModel(this@withEncryptionContext) }
                }
            }
        }
        .distinctUntilChanged()

    private val sortedListItemFlow: Flow<LoadingResult<List<GroupedItemList>>> = combine(
        itemUiModelFlow,
        sortingSelectionFlow
    ) { result, sortingType ->
        when (sortingType.searchSortingType) {
            SearchSortingType.TitleAsc -> result.map { list -> list.sortByTitleAsc() }
            SearchSortingType.TitleDesc -> result.map { list -> list.sortByTitleDesc() }
            SearchSortingType.CreationAsc -> result.map { list -> list.sortByCreationAsc() }
            SearchSortingType.CreationDesc -> result.map { list -> list.sortByCreationDesc() }
            SearchSortingType.MostRecent -> result.map { list -> list.sortByMostRecent(clock.now()) }
        }
    }.distinctUntilChanged()

    @OptIn(FlowPreview::class)
    private val textFilterListItemFlow: Flow<LoadingResult<List<GroupedItemList>>> = combine(
        sortedListItemFlow,
        searchQueryState.debounce(DEBOUNCE_TIMEOUT),
        isInSearchModeState
    ) { result, searchQuery, isInSearchMode ->
        isProcessingSearchState.update { IsProcessingSearchState.NotLoading }
        if (isInSearchMode && searchQuery.isNotBlank()) {
            result.map { grouped ->
                grouped.map {
                    GroupedItemList(
                        it.key,
                        ItemUiFilter.filterByQuery(it.items, searchQuery)
                    )
                }
            }
        } else {
            result
        }
    }.flowOn(Dispatchers.Default)

    private val suggestionsItemUIModelFlow: Flow<LoadingResult<List<ItemUiModel>>> =
        autofillAppState
            .flatMapLatest { state ->
                if (state is Some) {
                    getSuggestedLoginItems(
                        packageName = state.value.packageInfoUi?.packageName.toOption(),
                        url = state.value.webDomain
                    ).asResultWithoutLoading()
                } else {
                    flowOf(LoadingResult.Loading)
                }
            }
            .map { itemResult ->
                itemResult.map { list ->
                    encryptionContextProvider.withEncryptionContext {
                        list.map { it.toUiModel(this@withEncryptionContext) }
                    }
                }
            }

    private val resultsFlow: Flow<LoadingResult<SelectItemListItems>> = combine(
        autofillAppState,
        textFilterListItemFlow,
        suggestionsItemUIModelFlow,
        isInSearchModeState
    ) { autofillAppState, result, suggestionsResult, isInSearchMode ->
        result.map { grouped ->
            if (isInSearchMode) {
                SelectItemListItems(
                    suggestions = persistentListOf(),
                    items = grouped.map { GroupedItemList(it.key, it.items) }
                        .filter { it.items.isNotEmpty() }
                        .toImmutableList(),
                    suggestionsForTitle = ""
                )
            } else {
                val suggestions = suggestionsResult.getOrNull() ?: persistentListOf()
                val suggestionIds = suggestions.map { it.id }.toSet()
                SelectItemListItems(
                    suggestions = suggestions
                        .sortSuggestionsByMostRecent()
                        .toImmutableList(),
                    items = grouped
                        .map {
                            it.copy(
                                items = it.items.filter { item -> !suggestionIds.contains(item.id) }
                            )
                        }
                        .filter { it.items.isNotEmpty() }
                        .toImmutableList(),
                    suggestionsForTitle = autofillAppState.value()
                        ?.let(::getSuggestionsTitle)
                        ?: ""
                )
            }
        }
    }.flowOn(Dispatchers.Default)

    private val isRefreshing: MutableStateFlow<IsRefreshingState> =
        MutableStateFlow(IsRefreshingState.NotRefreshing)
    private val itemClickedFlow: MutableStateFlow<AutofillItemClickedEvent> =
        MutableStateFlow(AutofillItemClickedEvent.None)

    val uiState: StateFlow<SelectItemUiState> = combineN(
        resultsFlow,
        shareIdToSharesFlow,
        isRefreshing,
        itemClickedFlow,
        searchWrapper,
        sortingSelectionFlow,
        shouldScrollToTopFlow,
        preferenceRepository.getUseFaviconsPreference(),
        planTypeFlow,
        observeUpgradeInfo().asLoadingResult()
    ) { itemsResult,
        shares,
        isRefreshing,
        itemClicked,
        search,
        sortingSelection,
        shouldScrollToTop,
        useFavicons,
        plan,
        upgradeInfo ->
        val isLoading = IsLoadingState.from(itemsResult is LoadingResult.Loading)
        val items = when (itemsResult) {
            LoadingResult.Loading -> SelectItemListItems.Initial
            is LoadingResult.Success -> itemsResult.data
            is LoadingResult.Error -> {
                PassLogger.i(
                    TAG,
                    itemsResult.exception,
                    "Could not load autofill items"
                )
                snackbarDispatcher(LoadItemsError)
                SelectItemListItems.Initial
            }
        }

        val (displayOnlyPrimaryVaultMessage, searchIn) = when (plan) {
            is PlanType.Free -> (shares.size > 1) to SearchInMode.PrimaryVault
            else -> false to SearchInMode.AllVaults
        }

        val canUpgrade = upgradeInfo.getOrNull()?.isUpgradeAvailable ?: false

        SelectItemUiState(
            SelectItemListUiState(
                isLoading = isLoading,
                isRefreshing = isRefreshing,
                itemClickedEvent = itemClicked,
                items = items,
                shares = shares,
                sortingType = sortingSelection.searchSortingType,
                shouldScrollToTop = shouldScrollToTop,
                canLoadExternalImages = useFavicons.value(),
                displayOnlyPrimaryVaultMessage = displayOnlyPrimaryVaultMessage,
                canUpgrade = canUpgrade
            ),
            SearchUiState(
                searchQuery = search.searchQuery,
                inSearchMode = search.isInSearchMode,
                isProcessingSearch = search.isProcessingSearch,
                searchInMode = searchIn
            )
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SelectItemUiState.Loading
        )

    fun onItemClicked(
        item: ItemUiModel,
        autofillAppState: AutofillAppState,
        shouldAssociate: Boolean
    ) {
        item.toAutoFillItem()
            .map { autofillItem ->
                encryptionContextProvider.withEncryptionContext {
                    handleTotpUri(this@withEncryptionContext, autofillItem.totp)
                    updateAutofillItem(
                        UpdateAutofillItemData(
                            shareId = ShareId(autofillItem.shareId),
                            itemId = ItemId(autofillItem.itemId),
                            packageInfo = autofillAppState.packageInfoUi.toOption()
                                .map(PackageInfoUi::toPackageInfo),
                            url = autofillAppState.webDomain,
                            shouldAssociate = shouldAssociate
                        )
                    )

                    val mappings = ItemFieldMapper.mapFields(
                        encryptionContext = this@withEncryptionContext,
                        autofillItem = autofillItem,
                        androidAutofillFieldIds = autofillAppState.androidAutofillIds,
                        autofillTypes = autofillAppState.fieldTypes
                    )
                    itemClickedFlow.update {
                        AutofillItemClickedEvent.Clicked(mappings)
                    }
                }
            }
    }

    fun onSearchQueryChange(query: String) {
        if (query.contains("\n")) return

        searchQueryState.update { query }
        isProcessingSearchState.update { IsProcessingSearchState.Loading }
    }

    fun onStopSearching() {
        searchQueryState.update { "" }
        isInSearchModeState.update { false }
    }

    fun onEnterSearch() {
        searchQueryState.update { "" }
        isInSearchModeState.update { true }
    }

    fun setInitialState(autofillAppState: AutofillAppState) {
        this.autofillAppState.update { autofillAppState.toOption() }
    }

    fun onScrolledToTop() {
        shouldScrollToTopFlow.update { false }
    }

    private fun getSuggestionsTitle(autofillAppState: AutofillAppState): String =
        if (autofillAppState.webDomain is Some) {
            getSuggestionsTitleForDomain(autofillAppState.webDomain.value)
        } else if (autofillAppState.packageInfoUi != null) {
            autofillAppState.packageInfoUi.appName
        } else {
            ""
        }

    private fun getSuggestionsTitleForDomain(domain: String): String =
        UrlSanitizer.sanitize(domain).fold(
            onSuccess = {
                runCatching {
                    val parsed = URI(it)
                    parsed.host
                }.getOrDefault("")
            },
            onFailure = {
                PassLogger.i(TAG, it, "Error sanitizing URL [url=$domain]")
                ""
            }
        )

    private fun handleTotpUri(encryptionContext: EncryptionContext, totp: EncryptedString?) {
        if (totp == null) return

        val totpUri = encryptionContext.decrypt(totp)
        val copyTotpToClipboard = runBlocking {
            preferenceRepository.getCopyTotpToClipboardEnabled().first()
        }
        if (totpUri.isNotBlank() && copyTotpToClipboard.value()) {
            viewModelScope.launch {
                getTotpCodeFromUri(totpUri)
                    .onSuccess {
                        clipboardManager.copyToClipboard(it)
                        toastManager.showToast(R.string.autofill_notification_copy_to_clipboard)
                    }
                    .onFailure {
                        PassLogger.w(TAG, "Could not copy totp code")
                    }
            }
        }
    }

    companion object {
        private const val DEBOUNCE_TIMEOUT = 300L
        private const val TAG = "SelectItemViewModel"
    }
}
