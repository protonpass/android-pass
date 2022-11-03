package me.proton.pass.autofill.ui.autofill.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.android.pass.notifications.api.SnackbarMessageRepository
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.pass.autofill.BROWSERS
import me.proton.pass.autofill.extensions.toAutoFillItem
import me.proton.pass.autofill.extensions.toUiModel
import me.proton.pass.autofill.ui.autofill.select.SelectItemSnackbarMessage.LoadItemsError
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.Some
import me.proton.pass.common.api.map
import me.proton.pass.common.api.toOption
import me.proton.pass.data.usecases.UpdateAutofillItem
import me.proton.pass.data.usecases.UpdateAutofillItemData
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.entity.PackageName
import me.proton.pass.domain.usecases.RefreshContent
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.IsRefreshingState
import me.proton.pass.search.SearchItems
import javax.inject.Inject

@HiltViewModel
class SelectItemViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val accountManager: AccountManager,
    private val updateAutofillItem: UpdateAutofillItem,
    private val refreshContent: RefreshContent,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    private val searchItems: SearchItems
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val listItems: Flow<Result<List<ItemUiModel>>> = searchItems.observeResults()
        .mapLatest { result: Result<List<Item>> ->
            result.map { list ->
                list.filter { it.itemType is ItemType.Login }.map { it.toUiModel(cryptoContext) }
            }
        }

    private val webDomainFilterState: MutableStateFlow<Option<String>> = MutableStateFlow(None)

    private val searchQueryState: MutableStateFlow<String> = MutableStateFlow("")
    private val isInSearchModeState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val searchWrapper = combine(
        searchQueryState,
        isInSearchModeState
    ) { searchQuery, isInSearchMode -> SearchWrapper(searchQuery, isInSearchMode) }

    private data class SearchWrapper(
        val searchQuery: String,
        val isInSearchMode: Boolean
    )

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
            Result.Loading -> emptyList()
            is Result.Success -> itemsResult.data
            is Result.Error -> {
                val defaultMessage = "Could not load autofill items"
                PassLogger.i(
                    TAG,
                    itemsResult.exception ?: Exception(defaultMessage),
                    defaultMessage
                )
                snackbarMessageRepository.emitSnackbarMessage(LoadItemsError)
                emptyList()
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
                inSearchMode = search.isInSearchMode
            )
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SelectItemUiState.Loading
        )

    fun onItemClicked(item: ItemUiModel, packageName: PackageName) {
        val packageNameOption = packageName.takeIf { !BROWSERS.contains(packageName.packageName) }
            .toOption()
        val domain = webDomainFilterState.value
        if (packageNameOption is Some || domain is Some) {
            updateAutofillItem(
                shareId = item.shareId,
                itemId = item.id,
                data = UpdateAutofillItemData(packageNameOption, domain)
            )
        }

        itemClickedFlow.update {
            ItemClickedEvent.Clicked(item.toAutoFillItem(cryptoContext.keyStoreCrypto))
        }
    }

    fun onRefresh() = viewModelScope.launch(coroutineExceptionHandler) {
        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        if (userId != null) {
            isRefreshing.update { IsRefreshingState.Refreshing }
            refreshContent(userId)
            isRefreshing.update { IsRefreshingState.NotRefreshing }
        }
    }

    fun onSearchQueryChange(query: String) {
        if (query.contains("\n")) return

        searchQueryState.value = query
        searchItems.updateQuery(query)
    }

    fun onStopSearching() {
        searchItems.clearSearch()
        searchQueryState.update { "" }
        isInSearchModeState.update { false }
    }

    fun onEnterSearch() {
        searchItems.clearSearch()
        searchQueryState.update { "" }
        isInSearchModeState.update { true }
    }

    fun setWebDomain(domain: Option<String>) {
        webDomainFilterState.update { domain }
    }

    companion object {
        private const val TAG = "SelectItemViewModel"
    }
}
