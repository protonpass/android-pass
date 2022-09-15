package me.proton.android.pass.ui.trash

import androidx.compose.runtime.Immutable
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
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.android.pass.BuildConfig
import me.proton.android.pass.R
import me.proton.android.pass.extension.toUiModel
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.ItemState
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.ShareSelection
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.domain.usecases.ObserveItems
import me.proton.core.pass.domain.usecases.ObserveShares
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawerViewState
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class TrashScreenViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val accountManager: AccountManager,
    private val userManager: UserManager,
    private val observeItems: ObserveItems,
    private val observeShares: ObserveShares,
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val getCurrentUserIdFlow = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userManager.observeUser(it) }
        .distinctUntilChanged()

    private val listShares = getCurrentUserIdFlow
        .filterNotNull()
        .flatMapLatest { user ->
            observeShares(user.userId).map { shares ->
                shares.firstOrNull()?.id
            }
        }
        .distinctUntilChanged()

    private val listItems: Flow<List<ItemUiModel>> = getCurrentUserIdFlow
        .filterNotNull()
        .flatMapLatest { user ->
            observeItems(user.userId, ShareSelection.AllShares, ItemState.Trashed).map { items ->
                items.map { it.toUiModel(cryptoContext) }
            }
        }

    private val restoreItemState: MutableStateFlow<RequestState> = MutableStateFlow(RequestState.Success)
    private val deleteItemState: MutableStateFlow<RequestState> = MutableStateFlow(RequestState.Success)
    private val clearTrashState: MutableStateFlow<RequestState> = MutableStateFlow(RequestState.Success)
    private val requestState: Flow<RequestState> = combine(
        restoreItemState,
        deleteItemState,
        clearTrashState
    ) { restore, delete, clearTrash ->
        if (restore is RequestState.Loading || delete is RequestState.Loading || clearTrash is RequestState.Loading) {
            RequestState.Loading
        } else if (restore is RequestState.Error || delete is RequestState.Error || clearTrash is RequestState.Error) {
            RequestState.Error
        } else {
            RequestState.Success
        }
    }

    val initialNavDrawerState = NavigationDrawerViewState(
        R.string.app_name,
        BuildConfig.VERSION_NAME,
        currentUser = null
    )
    val navDrawerState: StateFlow<NavigationDrawerViewState> = getCurrentUserIdFlow
        .filterNotNull()
        .mapLatest { user ->
            NavigationDrawerViewState(
                R.string.app_name,
                BuildConfig.VERSION_NAME,
                currentUser = user
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = initialNavDrawerState
        )

    val uiState: StateFlow<State> = combine(
        listShares,
        listItems,
        requestState
    ) { shareId, items, request ->
        when (request) {
            is RequestState.Loading -> State.Loading
            is RequestState.Error -> State.Error("Error in request")
            is RequestState.Success -> State.Content(
                items,
                selectedShare = shareId
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = State.Loading
    )

    fun restoreItem(item: ItemUiModel) = viewModelScope.launch {
        withUserId {
            restoreItemState.value = RequestState.Loading
            itemRepository.untrashItem(it, item.shareId, item.id)
            restoreItemState.value = RequestState.Success
        }
    }

    fun deleteItem(item: ItemUiModel) = viewModelScope.launch {
        withUserId {
            deleteItemState.value = RequestState.Loading
            itemRepository.deleteItem(it, item.shareId, item.id)
            deleteItemState.value = RequestState.Success
        }
    }

    fun clearTrash() = viewModelScope.launch {
        withUserId {
            clearTrashState.value = RequestState.Loading
            itemRepository.clearTrash(it)
            clearTrashState.value = RequestState.Success
        }
    }

    private suspend fun withUserId(block: suspend (UserId) -> Unit) {
        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        userId?.let { block(it) }
    }

    @Immutable
    internal sealed class RequestState {
        object Loading: RequestState()
        object Success: RequestState()
        object Error: RequestState()
    }

    @Immutable
    sealed class State {
        object Loading: State()
        data class Content(
            val items: List<ItemUiModel>,
            val selectedShare: ShareId? = null
        ): State()
        data class Error(val message: String): State()
    }
}
