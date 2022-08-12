package me.proton.android.pass.ui.create.login

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.usecases.CreateItem

@HiltViewModel
class CreateLoginViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val createItem: CreateItem
) : BaseLoginViewModel() {

    fun createItem(shareId: ShareId) = viewModelScope.launch {
        viewState.value = viewState.value.copy(state = State.Loading)
        accountManager.getPrimaryUserId().first { userId -> userId != null }?.let { userId ->
            val itemContents = viewState.value.modelState.toItemContents()
            val createdItem = createItem(userId, shareId, itemContents)
            viewState.value = viewState.value.copy(state = State.Success(createdItem.id))
        }
    }
}
