package me.proton.core.pass.presentation.create.note

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.repositories.ItemRepository
import me.proton.core.pass.domain.usecases.GetShareById
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState
import javax.inject.Inject

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val getShare: GetShareById,
    private val itemRepository: ItemRepository
) : BaseNoteViewModel() {

    fun createNote(shareId: ShareId) = viewModelScope.launch {
        val noteItem = noteItemState.value
        val noteItemValidationErrors = noteItem.validate()
        if (noteItemValidationErrors.isNotEmpty()) {
            noteItemValidationErrorsState.value = noteItemValidationErrors
        } else {
            isLoadingState.value = IsLoadingState.Loading
            accountManager.getPrimaryUserId().first { userId -> userId != null }?.let { userId ->
                val share = getShare.invoke(userId, shareId)
                requireNotNull(share)
                val itemContents = noteItem.toItemContents()
                val createdItem = itemRepository.createItem(userId, share, itemContents)
                isLoadingState.value = IsLoadingState.NotLoading
                isItemSavedState.value = ItemSavedState.Success(createdItem.id)
            }
        }
    }
}
