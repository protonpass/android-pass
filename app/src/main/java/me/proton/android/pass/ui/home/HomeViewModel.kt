package me.proton.android.pass.ui.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.android.pass.BuildConfig
import me.proton.android.pass.R
import me.proton.android.pass.extension.toUiModel
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.pass.domain.usecases.ObserveActiveShare
import me.proton.core.pass.domain.usecases.ObserveCurrentUser
import me.proton.core.pass.domain.usecases.TrashItem
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawerViewState
import me.proton.core.pass.search.SearchItems
import javax.inject.Inject

@ExperimentalMaterialApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val trashItem: TrashItem,
    private val searchItems: SearchItems,
    observeCurrentUser: ObserveCurrentUser,
    observeActiveShare: ObserveActiveShare
) : ViewModel() {

    private val currentUserFlow = observeCurrentUser().filterNotNull()

    val initialNavDrawerState = NavigationDrawerViewState(
        R.string.app_name,
        BuildConfig.VERSION_NAME,
        currentUser = null
    )

    val navDrawerState: StateFlow<NavigationDrawerViewState> = currentUserFlow
        .mapLatest { user ->
            NavigationDrawerViewState(
                R.string.app_name,
                BuildConfig.VERSION_NAME,
                currentUser = user
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = initialNavDrawerState
        )


    private val listItems: Flow<List<ItemUiModel>> = searchItems.observeResults()
        .mapLatest { items -> items.map { it.toUiModel(cryptoContext) } }

    private val searchQuery: MutableStateFlow<String> = MutableStateFlow("")

    val viewState: Flow<HomeUiState> = combine(
        observeActiveShare(),
        listItems,
        searchQuery
    ) { shareId, items, searchQuery ->
        HomeUiState.Content(
            items,
            selectedShare = shareId,
            searchQuery = searchQuery
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading
    )

    fun onSearchQueryChange(query: String) = viewModelScope.launch {
        if (query.contains("\n")) return@launch

        searchQuery.value = query
        searchItems.updateQuery(query)
    }

    fun onStopSearching() = viewModelScope.launch {
        searchItems.clearSearch()
        searchQuery.value = ""
    }

    fun sendItemToTrash(item: ItemUiModel?) = viewModelScope.launch {
        if (item == null) return@launch

        val userId = currentUserFlow.firstOrNull()?.userId
        if (userId != null) {
            trashItem.invoke(userId, item.shareId, item.id)
        }
    }
}
