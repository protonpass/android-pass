package me.proton.android.pass.ui.create.item

import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.pass.domain.ItemContents
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.usecases.CreateItem
import me.proton.core.pass.domain.usecases.GetShareById
import javax.inject.Inject

@HiltViewModel
class CreateItemViewModel @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val accountManager: AccountManager,
    private val createItem: CreateItem,
    private val getShare: GetShareById,
) : ViewModel() {
    val initialViewState = State.Idle
    val loadingState: MutableStateFlow<State> = MutableStateFlow(State.Idle)

    fun createItem(shareId: String, model: CreateItemUiModel) = viewModelScope.launch {
        loadingState.value = State.Loading
        accountManager.getPrimaryUserId().first { userId -> userId != null }?.let { userId ->
            val share = getShare.invoke(userId, ShareId(shareId))
            requireNotNull(share)
            val createdItem = createItem.invoke(userId, share, model.toItemContents())
            loadingState.value = State.Success(createdItem.id)
        }
    }

    data class CreateItemUiModel(
        val title: String,
        val username: String,
        val password: String
    ) {
        fun toItemContents(): ItemContents = ItemContents.Login(
            title = title,
            username = username,
            password = password
        )
    }

    sealed class State {
        object Loading: State()
        object Idle: State()
        data class Error(val message: String): State()
        data class Success(val itemId: ItemId): State()
    }
}