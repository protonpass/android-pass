package proton.android.pass.autofill.ui.autofill.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import proton.android.pass.autofill.AutofillDisplayed
import proton.android.pass.autofill.AutofillTriggerSource
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.extensions.toAutoFillItem
import proton.android.pass.autofill.ui.autofill.ItemFieldMapper
import proton.android.pass.autofill.ui.autofill.select.SelectItemSnackbarMessage.LoadItemsError
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.common.api.flatMap
import proton.android.pass.common.api.map
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonui.api.GroupingKeys
import proton.android.pass.commonui.api.ItemUiFilter
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateAutofillItemData
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.NotificationManager
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.GetTotpCodeFromUri
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import java.net.URI
import javax.inject.Inject

@HiltViewModel
class SelectItemViewModel @Inject constructor(
    private val updateAutofillItem: UpdateAutofillItem,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val clipboardManager: ClipboardManager,
    private val getTotpCodeFromUri: GetTotpCodeFromUri,
    private val notificationManager: NotificationManager,
    private val preferenceRepository: UserPreferencesRepository,
    observeActiveItems: ObserveActiveItems,
    getSuggestedLoginItems: GetSuggestedLoginItems,
    telemetryManager: TelemetryManager
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

    private val searchWrapper = combine(
        searchQueryState,
        isInSearchModeState,
        isProcessingSearchState
    ) { searchQuery, isInSearchMode, isProcessingSearch ->
        SearchWrapper(searchQuery, isInSearchMode, isProcessingSearch)
    }

    private data class SearchWrapper(
        val searchQuery: String,
        val isInSearchMode: Boolean,
        val isProcessingSearch: IsProcessingSearchState
    )

    private val activeItemUIModelFlow: Flow<LoadingResult<List<ItemUiModel>>> =
        observeActiveItems(filter = ItemTypeFilter.Logins)
            .asResultWithoutLoading()
            .map { itemResult ->
                itemResult.map { list ->
                    encryptionContextProvider.withEncryptionContext {
                        list.map { it.toUiModel(this@withEncryptionContext) }
                    }
                }
            }
            .distinctUntilChanged()

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


    @OptIn(FlowPreview::class)
    private val listItems: Flow<LoadingResult<SelectItemListItems>> = combine(
        autofillAppState,
        activeItemUIModelFlow,
        suggestionsItemUIModelFlow,
        searchQueryState.debounce(DEBOUNCE_TIMEOUT)
    ) { autofillAppState, result, suggestionsResult, searchQuery ->
        isProcessingSearchState.update { IsProcessingSearchState.NotLoading }
        if (searchQuery.isNotBlank()) {
            itemsForSearch(result, searchQuery)
        } else {
            itemsWhenNotSearching(result, suggestionsResult, autofillAppState)
        }

    }.flowOn(Dispatchers.Default)

    private val isRefreshing: MutableStateFlow<IsRefreshingState> =
        MutableStateFlow(IsRefreshingState.NotRefreshing)
    private val itemClickedFlow: MutableStateFlow<AutofillItemClickedEvent> =
        MutableStateFlow(AutofillItemClickedEvent.None)

    val uiState: StateFlow<SelectItemUiState> = combine(
        listItems,
        isRefreshing,
        itemClickedFlow,
        searchWrapper
    ) { itemsResult, isRefreshing, itemClicked, search ->
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

        SelectItemUiState(
            SelectItemListUiState(
                isLoading = isLoading,
                isRefreshing = isRefreshing,
                itemClickedEvent = itemClicked,
                items = items
            ),
            SearchUiState(
                searchQuery = search.searchQuery,
                inSearchMode = search.isInSearchMode,
                isProcessingSearch = search.isProcessingSearch
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
                    val totpUri = decrypt(autofillItem.totp)
                    val copyTotpToClipboard = runBlocking {
                        preferenceRepository.getCopyTotpToClipboardEnabled().first()
                    }
                    if (totpUri.isNotBlank() && copyTotpToClipboard.value()) {
                        viewModelScope.launch {
                            getTotpCodeFromUri(totpUri)
                                .onSuccess {
                                    withContext(Dispatchers.IO) {
                                        clipboardManager.copyToClipboard(it)
                                    }
                                    notificationManager.sendNotification()
                                }
                                .onFailure {
                                    PassLogger.w(TAG, "Could not copy totp code")
                                }
                        }
                    }
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

    private fun itemsForSearch(
        result: LoadingResult<List<ItemUiModel>>,
        searchQuery: String
    ): LoadingResult<SelectItemListItems> =
        result
            .map { ItemUiFilter.filterByQuery(it, searchQuery) }
            .map { items ->
                SelectItemListItems(
                    suggestions = persistentListOf(),
                    items = persistentListOf(
                        GroupedItemList(
                            GroupingKeys.NoGrouping,
                            items.toImmutableList()
                        )
                    ),
                    suggestionsForTitle = ""
                )
            }

    private fun itemsWhenNotSearching(
        result: LoadingResult<List<ItemUiModel>>,
        suggestionsResult: LoadingResult<List<ItemUiModel>>,
        autofillAppState: Option<AutofillAppState>
    ): LoadingResult<SelectItemListItems> {
        return result.flatMap { items ->
            suggestionsResult.map { suggestions ->
                sortResults(items, suggestions, autofillAppState)
            }
        }
    }

    private fun sortResults(
        allItems: List<ItemUiModel>,
        suggestions: List<ItemUiModel>,
        autofillAppState: Option<AutofillAppState>
    ): SelectItemListItems {
        val sorted = ItemSuggestionSorter.sort(allItems, suggestions)
        return SelectItemListItems(
            items = persistentListOf(
                GroupedItemList(
                    GroupingKeys.NoGrouping,
                    sorted.allItems.toImmutableList()
                )
            ),
            suggestions = sorted.suggestions.toImmutableList(),
            suggestionsForTitle = autofillAppState.value()
                ?.let { getSuggestionsTitle(it) }
                ?: ""
        )
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

    companion object {
        private const val DEBOUNCE_TIMEOUT = 300L
        private const val TAG = "SelectItemViewModel"
    }
}
