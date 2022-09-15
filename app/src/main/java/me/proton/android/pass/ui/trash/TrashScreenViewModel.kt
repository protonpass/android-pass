package me.proton.android.pass.ui.trash

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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
import me.proton.core.user.domain.entity.User

@HiltViewModel
class TrashScreenViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val accountManager: AccountManager,
    private val userManager: UserManager,
    private val observeItems: ObserveItems,
    private val observeShares: ObserveShares,
    private val itemRepository: ItemRepository
) : ViewModel() {

    val initialViewState = getViewState(
        user = null,
        items = emptyList(),
        shareId = null
    )

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

    private val listItems = getCurrentUserIdFlow
        .filterNotNull()
        .flatMapLatest { user ->
            observeItems(user.userId, ShareSelection.AllShares, ItemState.Trashed).map { items ->
                items.map { it.toUiModel(cryptoContext) }
            }
        }

    val viewState: Flow<ViewState> = combine(
        getCurrentUserIdFlow,
        listShares,
        listItems,
        ::getViewState
    )

    private fun getViewState(
        user: User?,
        shareId: ShareId?,
        items: List<ItemUiModel>
    ): ViewState =
        ViewState(
            navigationDrawerViewState = NavigationDrawerViewState(
                R.string.app_name,
                BuildConfig.VERSION_NAME,
                currentUser = user
            ),
            items,
            selectedShare = shareId
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

    suspend fun withUserId(block: suspend (UserId) -> Unit) {
        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        userId?.let { block(it) }
    }

    @Immutable
    data class ViewState(
        val navigationDrawerViewState: NavigationDrawerViewState,
        val items: List<ItemUiModel>,
        val selectedShare: ShareId? = null
    )
}
