package me.proton.android.pass.ui.create.alias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.AliasMailbox
import me.proton.core.pass.domain.AliasOptions
import me.proton.core.pass.domain.AliasSuffix
import me.proton.core.pass.domain.ItemContents
import me.proton.core.pass.domain.ItemId

abstract class BaseAliasViewModel(
    private val accountManager: AccountManager
) : ViewModel() {

    val initialViewState = ViewState()
    val viewState: MutableStateFlow<ViewState> = MutableStateFlow(initialViewState)

    fun onTitleChange(value: String) = viewModelScope.launch {
        viewState.value =
            viewState.value.copy(modelState = viewState.value.modelState.copy(title = value))
    }

    fun onAliasChange(value: String) = viewModelScope.launch {
        if (value.contains(" ") || value.contains("\n")) return@launch
        viewState.value = viewState.value.copy(
            modelState = viewState.value.modelState.copy(
                alias = value,
                aliasToBeCreated = getAliasToBeCreated(
                    alias = value,
                    suffix = viewState.value.modelState.selectedSuffix
                )
            )
        )
    }

    fun onNoteChange(value: String) = viewModelScope.launch {
        viewState.value =
            viewState.value.copy(modelState = viewState.value.modelState.copy(note = value))
    }

    fun onSuffixChange(suffix: AliasSuffix) = viewModelScope.launch {
        viewState.value = viewState.value.copy(
            modelState = viewState.value.modelState.copy(
                selectedSuffix = suffix,
                aliasToBeCreated = getAliasToBeCreated(
                    alias = viewState.value.modelState.alias,
                    suffix = suffix
                )
            )
        )
    }

    fun onMailboxChange(mailbox: AliasMailbox) = viewModelScope.launch {
        viewState.value =
            viewState.value.copy(modelState = viewState.value.modelState.copy(selectedMailbox = mailbox))
    }

    protected suspend fun withUserId(block: suspend (UserId) -> Unit) {
        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        userId?.let { block(it) }
    }

    private fun getAliasToBeCreated(alias: String, suffix: AliasSuffix?): String? {
        if (suffix != null && alias.isNotBlank()) {
            return "$alias${suffix.suffix}"
        }
        return null
    }

    data class ModelState(
        val title: String = "",
        val alias: String = "",
        val note: String = "",
        val aliasOptions: AliasOptions = AliasOptions(emptyList(), emptyList()),
        val selectedSuffix: AliasSuffix? = null,
        val selectedMailbox: AliasMailbox? = null,
        val aliasToBeCreated: String? = null
    ) {
        fun toItemContents(): ItemContents {
            return ItemContents.Alias(
                title = title,
                note = note
            )
        }
    }

    data class ViewState(
        val state: State = State.Idle,
        val modelState: ModelState = ModelState()
    )

    sealed class State {
        object Loading : State()
        object Idle : State()
        data class Error(val message: String) : State()
        data class Success(val itemId: ItemId) : State()
    }
}
