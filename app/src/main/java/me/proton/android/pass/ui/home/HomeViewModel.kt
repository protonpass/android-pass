package me.proton.android.pass.ui.home

import androidx.compose.material.ExperimentalMaterialApi
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
import me.proton.core.pass.domain.ItemState
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.ShareSelection
import me.proton.core.pass.domain.usecases.ObserveItems
import me.proton.core.pass.domain.usecases.ObserveShares
import me.proton.core.pass.domain.usecases.TrashItem
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.components.navigation.drawer.NavigationDrawerViewState
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User

@ExperimentalMaterialApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val accountManager: AccountManager,
    private val userManager: UserManager,
    private val trashItem: TrashItem,
    private val observeShares: ObserveShares,
    private val observeItems: ObserveItems,
) : ViewModel() {

    val initialViewState = getViewState(
        user = null,
        items = emptyList(),
        shareId = null,
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

    private val listItems = listShares
        .filterNotNull()
        .combine(getCurrentUserIdFlow.filterNotNull()) { share, user -> share to user }
        .flatMapLatest { v ->
            observeItems(v.second.userId, ShareSelection.Share(v.first), ItemState.Active).map { items ->
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
        items: List<ItemUiModel>,
    ): ViewState =
        ViewState(
            navigationDrawerViewState = NavigationDrawerViewState(
                R.string.app_name,
                BuildConfig.VERSION_NAME,
                currentUser = user
            ),
            items,
            selectedShare = shareId,
        )

    fun sendItemToTrash(item: ItemUiModel?) = viewModelScope.launch {
        if (item == null) return@launch

        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        if (userId != null) {
            trashItem.invoke(userId, item.shareId, item.id)
        }
    }

    @Immutable
    data class ViewState(
        val navigationDrawerViewState: NavigationDrawerViewState,
        val items: List<ItemUiModel>,
        val selectedShare: ShareId? = null
    )
}
