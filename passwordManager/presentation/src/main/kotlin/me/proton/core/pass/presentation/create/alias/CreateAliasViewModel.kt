package me.proton.core.pass.presentation.create.alias

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.domain.AliasOptions
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.entity.NewAlias
import me.proton.core.pass.domain.repositories.AliasRepository
import me.proton.core.pass.domain.usecases.CreateAlias
import me.proton.core.pass.presentation.uievents.IsLoadingState
import me.proton.core.pass.presentation.uievents.ItemSavedState
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
            when (val result = aliasRepository.getAliasOptions(userId, shareId)) {
                is Result.Success -> {
                    _aliasOptions = result.data

                    isLoadingState.value = IsLoadingState.NotLoading
                    aliasItemState.value = aliasItemState.value.copy(
                        aliasOptions = result.data,
                        selectedSuffix = result.data.suffixes.first(),
                        selectedMailbox = result.data.mailboxes.first()
                    )
                }
                else -> {
                    // no-op
                }
            }
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
