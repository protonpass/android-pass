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
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.pass.autofill.entities.AutofillItem
import me.proton.core.pass.autofill.extensions.toAutoFillItem
import me.proton.core.pass.autofill.extensions.toUiModel
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.search.SearchItems
import javax.inject.Inject

@HiltViewModel
class SelectItemViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    searchItems: SearchItems
) : ViewModel() {

    private val listItems: Flow<List<ItemUiModel>> = searchItems.observeResults()
        .mapLatest { items ->
            items.filter { it.itemType is ItemType.Login }.map { it.toUiModel(cryptoContext) }
        }

    private val itemClickedFlow: MutableStateFlow<ItemClickedEvent> =
        MutableStateFlow(ItemClickedEvent.None)

    val uiState: StateFlow<SelectItemUiState> = combine(
        listItems,
        itemClickedFlow
    ) { items, itemClicked ->
        when (itemClicked) {
            is ItemClickedEvent.None -> SelectItemUiState.Content(items)
            is ItemClickedEvent.Clicked -> SelectItemUiState.Selected(itemClicked.item)
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SelectItemUiState.Loading
        )

    fun onItemClicked(item: ItemUiModel) = viewModelScope.launch {
        itemClickedFlow.value =
            ItemClickedEvent.Clicked(item.toAutoFillItem(cryptoContext.keyStoreCrypto))
    }

    internal sealed interface ItemClickedEvent {
        object None : ItemClickedEvent
        data class Clicked(val item: AutofillItem) : ItemClickedEvent
    }
}
