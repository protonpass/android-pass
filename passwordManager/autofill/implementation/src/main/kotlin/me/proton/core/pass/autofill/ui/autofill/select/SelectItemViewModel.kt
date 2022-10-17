package me.proton.core.pass.autofill.ui.autofill.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.pass.autofill.entities.AutofillItem
import me.proton.core.pass.autofill.extensions.toAutoFillItem
import me.proton.core.pass.autofill.extensions.toUiModel
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.common.api.map
import me.proton.core.pass.data.usecases.AddPackageNameToItem
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.entity.PackageName
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.search.SearchItems
import javax.inject.Inject

@HiltViewModel
class SelectItemViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val addPackageNameToItem: AddPackageNameToItem,
    searchItems: SearchItems
) : ViewModel() {

    private val listItems: Flow<Result<List<ItemUiModel>>> = searchItems.observeResults()
        .mapLatest { result: Result<List<Item>> ->
            result.map { list ->
                list.filter { it.itemType is ItemType.Login }.map { it.toUiModel(cryptoContext) }
            }
        }

    private val itemClickedFlow: MutableStateFlow<ItemClickedEvent> =
        MutableStateFlow(ItemClickedEvent.None)

    val uiState: StateFlow<SelectItemUiState> = combine(
        listItems,
        itemClickedFlow
    ) { itemsResult, itemClicked ->
        when (itemClicked) {
            is ItemClickedEvent.None -> {
                when (itemsResult) {
                    is Result.Success -> SelectItemUiState.Content(itemsResult.data)
                    is Result.Error -> {
                        val defaultMessage = "Could not load selected item list"
                        PassLogger.i(
                            TAG,
                            itemsResult.exception ?: Exception(defaultMessage),
                            defaultMessage
                        )
                        SelectItemUiState.Error(defaultMessage)
                    }
                    Result.Loading -> SelectItemUiState.Loading
                }
            }
            is ItemClickedEvent.Clicked -> SelectItemUiState.Selected(itemClicked.item)
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SelectItemUiState.Loading
        )

    fun onItemClicked(item: ItemUiModel, packageName: PackageName) = viewModelScope.launch {
        addPackageNameToItem(item.shareId, item.id, packageName)
        itemClickedFlow.value =
            ItemClickedEvent.Clicked(item.toAutoFillItem(cryptoContext.keyStoreCrypto))
    }

    internal sealed interface ItemClickedEvent {
        object None : ItemClickedEvent
        data class Clicked(val item: AutofillItem) : ItemClickedEvent
    }

    companion object {
        private const val TAG = "SelectItemViewModel"
    }
}
