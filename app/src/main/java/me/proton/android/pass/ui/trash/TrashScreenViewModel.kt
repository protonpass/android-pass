package me.proton.android.pass.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
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
import me.proton.core.pass.domain.ShareSelection
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.domain.usecases.ObserveItems
import me.proton.core.pass.domain.usecases.ObserveShares
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawerViewState
import me.proton.core.user.domain.UserManager
import me.proton.core.util.kotlin.CoreLogger
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

    val uiState: StateFlow<TrashUiState> = combine(
        listShares,
        listItems,
    ) { shareId, items ->
        CoreLogger.i("uiState", "ShareId: ${shareId} | Items: ${items}")
        TrashUiState.Content(
            items,
            selectedShare = shareId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrashUiState.Loading
    )

    fun restoreItem(item: ItemUiModel) = viewModelScope.launch {
        withUserId {
            itemRepository.untrashItem(it, item.shareId, item.id)
        }
    }

    fun deleteItem(item: ItemUiModel) = viewModelScope.launch {
        withUserId {
            itemRepository.deleteItem(it, item.shareId, item.id)
        }
    }

    fun clearTrash() = viewModelScope.launch {
        withUserId {
            itemRepository.clearTrash(it)
        }
    }

    private suspend fun withUserId(block: suspend (UserId) -> Unit) {
        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        userId?.let { block(it) }
    }
}