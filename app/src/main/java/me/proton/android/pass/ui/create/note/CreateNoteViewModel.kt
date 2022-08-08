package me.proton.android.pass.ui.create.note

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.domain.usecases.GetShareById

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val getShare: GetShareById,
    private val itemRepository: ItemRepository
) : BaseNoteViewModel() {

    fun createNote(shareId: ShareId) = viewModelScope.launch {
        viewState.value = viewState.value.copy(state = State.Loading)
        accountManager.getPrimaryUserId().first { userId -> userId != null }?.let { userId ->
            val share = getShare.invoke(userId, shareId)
            requireNotNull(share)
            val itemContents = viewState.value.modelState.toItemContents()
            val createdItem = itemRepository.createItem(userId, share, itemContents)
            viewState.value = viewState.value.copy(state = State.Success(createdItem.id))
        }
    }
}
