package me.proton.core.pass.autofill.ui.autofill.select

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
import me.proton.core.pass.autofill.extensions.toAutoFillItem
import me.proton.core.pass.autofill.extensions.toUiModel
import me.proton.core.pass.autofill.ui.autofill.select.SelectItemSnackbarMessage.LoadItemsError
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.common.api.map
import me.proton.core.pass.data.usecases.AddPackageNameToItem
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.entity.PackageName
import me.proton.core.pass.domain.usecases.RefreshContent
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.IsRefreshingState
import me.proton.core.pass.search.SearchItems
import javax.inject.Inject

@HiltViewModel
class SelectItemViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val accountManager: AccountManager,
    private val addPackageNameToItem: AddPackageNameToItem,
    private val refreshContent: RefreshContent,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    searchItems: SearchItems
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

    private val isRefreshing: MutableStateFlow<IsRefreshingState> =
        MutableStateFlow(IsRefreshingState.NotRefreshing)
    private val itemClickedFlow: MutableStateFlow<ItemClickedEvent> =
        MutableStateFlow(ItemClickedEvent.None)

    val uiState: StateFlow<SelectItemUiState> = combine(
        listItems,
        isRefreshing,
        itemClickedFlow
    ) { itemsResult, isRefreshing, itemClicked ->
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
            isLoading = isLoading,
            isRefreshing = isRefreshing,
            itemClickedEvent = itemClicked,
            items = items
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SelectItemUiState.Loading
        )

    fun onItemClicked(item: ItemUiModel, packageName: PackageName) {
        addPackageNameToItem(item.shareId, item.id, packageName)
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

    companion object {
        private const val TAG = "SelectItemViewModel"
    }
}
