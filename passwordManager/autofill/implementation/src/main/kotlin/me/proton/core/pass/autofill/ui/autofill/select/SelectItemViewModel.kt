package me.proton.core.pass.autofill.ui.autofill.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.pass.autofill.entities.AutofillItem
import me.proton.core.pass.autofill.extensions.toAutoFillItem
import me.proton.core.pass.autofill.extensions.toUiModel
import me.proton.core.pass.common.api.None
import me.proton.core.pass.common.api.Option
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

    private val isRefreshing: MutableStateFlow<IsRefreshingState> = MutableStateFlow(IsRefreshingState.NotRefreshing)
    private val _itemClickedFlow: MutableStateFlow<ItemClickedEvent> = MutableStateFlow(ItemClickedEvent.None)

    val itemClickedState: StateFlow<ItemClickedEvent> = _itemClickedFlow.asStateFlow()

    val uiState: StateFlow<SelectItemUiState> = combine(
        listItems,
        isRefreshing
    ) { itemsResult, isRefreshing ->
        val isLoading = IsLoadingState.from(itemsResult is Result.Loading)
        val (items, errorMessage) = when (itemsResult) {
            Result.Loading -> emptyList<ItemUiModel>() to None
            is Result.Success -> itemsResult.data to None
            is Result.Error -> {
                val defaultMessage = "Could not load autofill items"
                PassLogger.i(TAG, itemsResult.exception ?: Exception(defaultMessage), defaultMessage)
                emptyList<ItemUiModel>() to Option.fromNullable(itemsResult.exception?.message)
            }
        }

        SelectItemUiState(
            isLoading = isLoading,
            isRefreshing = isRefreshing,
            items = items,
            errorMessage = errorMessage
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SelectItemUiState.Loading
        )

    fun onItemClicked(item: ItemUiModel, packageName: PackageName) {
        addPackageNameToItem(item.shareId, item.id, packageName)
        _itemClickedFlow.update {
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

    sealed interface ItemClickedEvent {
        object None : ItemClickedEvent
        data class Clicked(val item: AutofillItem) : ItemClickedEvent
    }

    companion object {
        private const val TAG = "SelectItemViewModel"
    }
}
