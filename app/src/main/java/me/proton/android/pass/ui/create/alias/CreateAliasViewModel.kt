package me.proton.android.pass.ui.create.alias

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.proton.android.pass.ui.shared.uievents.IsLoadingState
import me.proton.android.pass.ui.shared.uievents.ItemSavedState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.pass.domain.AliasOptions
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.entity.NewAlias
import me.proton.core.pass.domain.repositories.AliasRepository
import me.proton.core.pass.domain.usecases.CreateAlias
import javax.inject.Inject

@HiltViewModel
class CreateAliasViewModel @Inject constructor(
    accountManager: AccountManager,
    private val aliasRepository: AliasRepository,
    private val createAlias: CreateAlias
) : BaseAliasViewModel(accountManager) {

    private var _aliasOptions: AliasOptions? = null

    fun onStart(shareId: ShareId) = viewModelScope.launch {
        if (_aliasOptions != null) return@launch
        isLoadingState.value = IsLoadingState.Loading
        withUserId { userId ->
            val aliasOptions = aliasRepository.getAliasOptions(userId, shareId)
            _aliasOptions = aliasOptions

            isLoadingState.value = IsLoadingState.NotLoading
            aliasItemState.value = aliasItemState.value.copy(
                aliasOptions = aliasOptions,
                selectedSuffix = aliasOptions.suffixes.first(),
                selectedMailbox = aliasOptions.mailboxes.first()
            )
        }
    }

    fun createAlias(shareId: ShareId) = viewModelScope.launch {
        val aliasItem = aliasItemState.value
        if (aliasItem.selectedMailbox == null || aliasItem.selectedSuffix == null) return@launch

        val aliasItemValidationErrors = aliasItem.validate()
        if (aliasItemValidationErrors.isNotEmpty()) {
            aliasItemValidationErrorsState.value = aliasItemValidationErrors
        } else {
            isLoadingState.value = IsLoadingState.Loading
            withUserId { userId ->
                val item = createAlias(
                    userId, shareId,
                    NewAlias(
                        title = aliasItem.title,
                        note = aliasItem.note,
                        prefix = aliasItem.alias,
                        suffix = aliasItem.selectedSuffix,
                        mailbox = aliasItem.selectedMailbox
                    )
                )
                isLoadingState.value = IsLoadingState.NotLoading
                isItemSavedState.value = ItemSavedState.Success(item.id)
            }
        }
    }
}
