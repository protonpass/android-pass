package me.proton.core.pass.presentation.create.alias

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.pass.common.api.onError
import me.proton.core.pass.common.api.onSuccess
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
            aliasRepository.getAliasOptions(userId, shareId)
                .onSuccess { aliasOptions ->
                    _aliasOptions = aliasOptions

                    val mailboxes = aliasOptions.mailboxes.mapIndexed { idx, model ->
                        AliasMailboxUiModel(model = model, selected = idx == 0)
                    }
                    val mailboxTitle = mailboxes.first { it.selected }.model.email

                    isLoadingState.value = IsLoadingState.NotLoading
                    aliasItemState.value = aliasItemState.value.copy(
                        aliasOptions = aliasOptions,
                        selectedSuffix = aliasOptions.suffixes.first(),
                        mailboxes = mailboxes,
                        mailboxTitle = mailboxTitle,
                        isMailboxListApplicable = true,
                    )
                }
                .onError {
                    val defaultMessage = "Could not get alias options"
                    PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                }
        }
    }

    fun createAlias(shareId: ShareId) = viewModelScope.launch {
        val aliasItem = aliasItemState.value
        if (aliasItem.selectedSuffix == null) return@launch

        val mailboxes = aliasItem.mailboxes.filter { it.selected }.map { it.model }
        val aliasItemValidationErrors = aliasItem.validate()
        if (aliasItemValidationErrors.isNotEmpty()) {
            aliasItemValidationErrorsState.value = aliasItemValidationErrors
        } else {
            isLoadingState.value = IsLoadingState.Loading
            withUserId { userId ->
                createAlias(
                    userId = userId,
                    shareId = shareId,
                    newAlias = NewAlias(
                        title = aliasItem.title,
                        note = aliasItem.note,
                        prefix = aliasItem.alias,
                        suffix = aliasItem.selectedSuffix,
                        mailboxes = mailboxes
                    )
                )
                    .onSuccess { item ->
                        isLoadingState.value = IsLoadingState.NotLoading
                        isItemSavedState.value = ItemSavedState.Success(item.id)
                    }
                    .onError {
                        val defaultMessage = "Create alias error"
                        PassLogger.i(TAG, it ?: Exception(defaultMessage), defaultMessage)
                    }
            }
        }
    }

    companion object {
        private const val TAG = "CreateAliasViewModel"
    }
}
