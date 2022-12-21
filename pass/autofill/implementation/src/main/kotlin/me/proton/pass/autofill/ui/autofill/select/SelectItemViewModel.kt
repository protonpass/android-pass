package me.proton.pass.autofill.ui.autofill.select

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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import me.proton.android.pass.data.api.UrlSanitizer
import me.proton.android.pass.data.api.crypto.EncryptionContextProvider
import me.proton.android.pass.data.api.usecases.GetAppNameFromPackageName
import me.proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import me.proton.android.pass.data.api.usecases.ItemTypeFilter
import me.proton.android.pass.data.api.usecases.ObserveActiveItems
import me.proton.android.pass.data.api.usecases.UpdateAutofillItem
import me.proton.android.pass.data.api.usecases.UpdateAutofillItemData
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.pass.autofill.BROWSERS
import me.proton.pass.autofill.extensions.toAutoFillItem
import me.proton.pass.autofill.ui.autofill.select.SelectItemSnackbarMessage.LoadItemsError
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.Some
import me.proton.pass.common.api.flatMap
import me.proton.pass.common.api.map
import me.proton.pass.common.api.toOption
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.extension.toUiModel
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.IsProcessingSearchState
import me.proton.pass.presentation.uievents.IsRefreshingState
import me.proton.pass.presentation.utils.ItemUiFilter
import java.net.URI
import javax.inject.Inject

@HiltViewModel
class SelectItemViewModel @Inject constructor(
    private val updateAutofillItem: UpdateAutofillItem,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val getAppNameFromPackageName: GetAppNameFromPackageName,
    private val encryptionContextProvider: EncryptionContextProvider,
    observeActiveItems: ObserveActiveItems,
    getSuggestedLoginItems: GetSuggestedLoginItems
) : ViewModel() {

    private val initialState: MutableStateFlow<SelectItemInitialState?> =
        MutableStateFlow(null)

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

    private val activeItemUIModelFlow: Flow<Result<List<ItemUiModel>>> =
        observeActiveItems(filter = ItemTypeFilter.Logins)
            .map { itemResult ->
                itemResult.map { list ->
                    encryptionContextProvider.withEncryptionContext {
                        list.map { it.toUiModel(this@withEncryptionContext) }
                    }
                }
            }
            .distinctUntilChanged()

    private val suggestionsItemUIModelFlow: Flow<Result<List<ItemUiModel>>> = initialState
        .flatMapLatest { state ->
            if (state == null) {
                flowOf(Result.Loading)
            } else {
                val packageName = if (BROWSERS.contains(state.packageName.packageName)) {
                    None
                } else {
                    Some(state.packageName.packageName)
                }
                getSuggestedLoginItems(packageName, state.webDomain)
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
    private val listItems: Flow<Result<SelectItemListItems>> = combine(
        activeItemUIModelFlow,
        suggestionsItemUIModelFlow,
        searchQueryState.debounce(DEBOUNCE_TIMEOUT)
    ) { result, suggestionsResult, searchQuery ->
        isProcessingSearchState.update { IsProcessingSearchState.NotLoading }
        if (searchQuery.isNotBlank()) {
            result
                .map { ItemUiFilter.filterByQuery(it, searchQuery) }
                .map { items ->
                    SelectItemListItems(
                        suggestions = persistentListOf(),
                        items = items.toImmutableList(),
                        suggestionsForTitle = ""
                    )
                }
        } else {
            result.flatMap { items ->
                suggestionsResult.map { suggestions ->
                    SelectItemListItems(
                        items = items.toImmutableList(),
                        suggestions = suggestions.toImmutableList(),
                        suggestionsForTitle = getSuggestionsTitle()
                    )
                }
            }
        }

    }.flowOn(Dispatchers.Default)

    private val isRefreshing: MutableStateFlow<IsRefreshingState> =
        MutableStateFlow(IsRefreshingState.NotRefreshing)
    private val itemClickedFlow: MutableStateFlow<ItemClickedEvent> =
        MutableStateFlow(ItemClickedEvent.None)

    val uiState: StateFlow<SelectItemUiState> = combine(
        listItems,
        isRefreshing,
        itemClickedFlow,
        searchWrapper
    ) { itemsResult, isRefreshing, itemClicked, search ->
        val isLoading = IsLoadingState.from(itemsResult is Result.Loading)
        val items = when (itemsResult) {
            Result.Loading -> SelectItemListItems.Initial
            is Result.Success -> itemsResult.data
            is Result.Error -> {
                val defaultMessage = "Could not load autofill items"
                PassLogger.i(
                    TAG,
                    itemsResult.exception ?: Exception(defaultMessage),
                    defaultMessage
                )
                snackbarMessageRepository.emitSnackbarMessage(LoadItemsError)
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

    fun onItemClicked(item: ItemUiModel) {
        val state = initialState.value ?: return

        val packageNameOption = state.packageName
            .takeIf { !BROWSERS.contains(state.packageName.packageName) }
            .toOption()
        if (packageNameOption is Some || state.webDomain is Some) {
            updateAutofillItem(
                shareId = item.shareId,
                itemId = item.id,
                data = UpdateAutofillItemData(packageNameOption, state.webDomain)
            )
        }

        encryptionContextProvider.withEncryptionContext {
            itemClickedFlow.update {
                ItemClickedEvent.Clicked(item.toAutoFillItem(this@withEncryptionContext))
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

    fun setInitialState(state: SelectItemInitialState) {
        initialState.update { state }
    }

    private fun getSuggestionsTitle(): String {
        val state = initialState.value ?: return ""
        return if (BROWSERS.contains(state.packageName.packageName)) {
            if (state.webDomain is Some) {
                getSuggestionsTitleForDomain(state.webDomain.value)
            } else {
                PassLogger.i(TAG, "Received autofill suggestion with only browser package name and no domain")
                ""
            }
        } else {
            getAppNameFromPackageName(state.packageName)
        }
    }

    private fun getSuggestionsTitleForDomain(domain: String): String =
        when (val sanitized = UrlSanitizer.sanitize(domain)) {
            Result.Loading -> ""
            is Result.Error -> {
                val message = "Error sanitizing URL [url=$domain]"
                PassLogger.i(TAG, sanitized.exception ?: Exception(message), message)
                ""
            }
            is Result.Success -> {
                runCatching {
                    val parsed = URI(sanitized.data)
                    parsed.host
                }.getOrDefault("")
            }
        }

    companion object {
        private const val DEBOUNCE_TIMEOUT = 300L
        private const val TAG = "SelectItemViewModel"
    }
}
