package me.proton.android.pass.ui.create.alias

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.pass.domain.AliasOptions
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.entity.NewAlias
import me.proton.core.pass.domain.repositories.AliasRepository
import me.proton.core.pass.domain.usecases.CreateAlias

@HiltViewModel
class CreateAliasViewModel @Inject constructor(
    accountManager: AccountManager,
    private val aliasRepository: AliasRepository,
    private val createAlias: CreateAlias
) : BaseAliasViewModel(accountManager) {

    private var _aliasOptions: AliasOptions? = null

    fun onStart(shareId: ShareId) = viewModelScope.launch {
        if (_aliasOptions != null) return@launch

        viewState.value = viewState.value.copy(state = State.Loading)
        withUserId { userId ->
            val aliasOptions = aliasRepository.getAliasOptions(userId, shareId)
            _aliasOptions = aliasOptions

            viewState.value = viewState.value.copy(
                state = State.Idle,
                modelState = viewState.value.modelState.copy(
                    aliasOptions = aliasOptions,
                    selectedSuffix = aliasOptions.suffixes.first(),
                    selectedMailbox = aliasOptions.mailboxes.first()
                )
            )
        }
    }

    fun createAlias(shareId: ShareId) = viewModelScope.launch {
        val modelState = viewState.value.modelState
        if (modelState.selectedMailbox == null || modelState.selectedSuffix == null || modelState.alias.isBlank()) return@launch

        viewState.value = viewState.value.copy(state = State.Loading)
        withUserId { userId ->
            val item = createAlias(
                userId, shareId,
                NewAlias(
                    title = modelState.title,
                    note = modelState.note,
                    prefix = modelState.alias,
                    suffix = modelState.selectedSuffix,
                    mailbox = modelState.selectedMailbox
                )
            )
            viewState.value = viewState.value.copy(state = State.Success(item.id))
        }
    }
}
