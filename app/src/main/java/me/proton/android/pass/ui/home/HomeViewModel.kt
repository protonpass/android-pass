package me.proton.android.pass.ui.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.proton.android.pass.BuildConfig
import me.proton.android.pass.R
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.pass.data.extensions.name
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.ShareSelection
import me.proton.core.pass.domain.usecases.DeleteItem
import me.proton.core.pass.domain.usecases.ObserveItems
import me.proton.core.pass.domain.usecases.ObserveShares
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.components.model.ShareUiModel
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawerViewEvent
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawerViewState
import me.proton.core.pass.presentation.components.navigation.drawer.ShareClickEvent
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User

@ExperimentalMaterialApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val accountManager: AccountManager,
    private val userManager: UserManager,
    private val deleteItem: DeleteItem,
    private val observeShares: ObserveShares,
    private val observeItems: ObserveItems,
) : ViewModel() {

    val initialViewState = getViewState(
        user = null,
        shares = emptyList(),
        items = emptyList(),
        shareSelection = ShareSelectionState(ShareSelection.AllShares, TopBarTitle.AllShares),
    )

    private val shareSelectionState: MutableStateFlow<ShareSelectionState> =
        MutableStateFlow(ShareSelectionState(ShareSelection.AllShares, TopBarTitle.AllShares))

    private val getCurrentUserIdFlow = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest { userManager.observeUser(it) }
        .distinctUntilChanged()

    private val listShares = getCurrentUserIdFlow
        .filterNotNull()
        .flatMapLatest { user ->
            observeShares(user.userId).map { shares ->
                shares.map { shareToShareUiModel(it) }
            }
        }
        .distinctUntilChanged()

    private val listItems = getCurrentUserIdFlow
        .filterNotNull()
        .combine(shareSelectionState) { user, shareSelection -> user to shareSelection }
        .flatMapLatest { v ->
            observeItems(v.first.userId, v.second.selection).map { items ->
                items.map { itemToItemUiModel(it) }
            }
        }

    val viewState: Flow<ViewState> = combine(
        getCurrentUserIdFlow,
        listShares,
        listItems,
        shareSelectionState,
        ::getViewState
    )

    private fun getViewState(
        user: User?,
        shares: List<ShareUiModel>,
        items: List<ItemUiModel>,
        shareSelection: ShareSelectionState
    ): ViewState {
        return ViewState(
            navigationDrawerViewState = NavigationDrawerViewState(
                R.string.app_name,
                BuildConfig.VERSION_NAME,
                currentUser = user
            ),
            shares,
            items,
            topBarTitle = shareSelection.title,
            selectedShare = when (val selection = shareSelection.selection) {
                is ShareSelection.Share -> selection.shareId
                else -> null
            }
        )
    }

    fun viewEvent(
        navigateToSigningOut: () -> Unit,
    ): ViewEvent = object : ViewEvent {
        override val navigationDrawerViewEvent: NavigationDrawerViewEvent =
            object : NavigationDrawerViewEvent {
                override val onSettings = {}
                override val onSignOut = navigateToSigningOut
                override val onHelp = {}
                override val onShareSelected: (ShareClickEvent) -> Unit = { share ->
                    when (share) {
                        is ShareClickEvent.AllShares -> {
                            shareSelectionState.value =
                                ShareSelectionState(ShareSelection.AllShares, TopBarTitle.AllShares)
                        }
                        is ShareClickEvent.Share -> {
                            shareSelectionState.value = ShareSelectionState(
                                ShareSelection.Share(share.share.id),
                                TopBarTitle.ShareName(share.share.name)
                            )
                        }
                    }
                }
            }
    }

    fun deleteItem(item: ItemUiModel?) = viewModelScope.launch {
        if (item != null) {
            val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
            if (userId != null) {
                deleteItem.invoke(userId, item.shareId, item.id)
            }
        }
    }

    private fun shareToShareUiModel(share: Share): ShareUiModel =
        ShareUiModel(
            id = share.id,
            name = share.name(cryptoContext)
        )

    private fun itemToItemUiModel(item: Item): ItemUiModel =
        ItemUiModel(
            id = item.id,
            shareId = item.shareId,
            name = item.name(cryptoContext),
            itemType = item.itemType,
        )

    @Immutable
    data class ViewState(
        val navigationDrawerViewState: NavigationDrawerViewState,
        val shares: List<ShareUiModel>,
        val items: List<ItemUiModel>,
        val topBarTitle: TopBarTitle,
        val selectedShare: ShareId? = null
    )

    data class ShareSelectionState(
        val selection: ShareSelection,
        val title: TopBarTitle
    )

    sealed class TopBarTitle {
        object AllShares : TopBarTitle()
        data class ShareName(val name: String) : TopBarTitle()
    }

    interface ViewEvent {
        val navigationDrawerViewEvent: NavigationDrawerViewEvent
    }
}
