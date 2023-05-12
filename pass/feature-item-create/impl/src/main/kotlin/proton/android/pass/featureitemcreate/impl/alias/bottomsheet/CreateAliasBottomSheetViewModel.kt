package proton.android.pass.featureitemcreate.impl.alias.bottomsheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.ObserveAliasOptions
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.featureitemcreate.impl.alias.AliasDraftSavedState
import proton.android.pass.featureitemcreate.impl.alias.AliasItem
import proton.android.pass.featureitemcreate.impl.alias.CreateAliasViewModel
import proton.android.pass.featureitemcreate.impl.alias.IsEditAliasNavArg
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class CreateAliasBottomSheetViewModel @Inject constructor(
    accountManager: AccountManager,
    createAlias: CreateAlias,
    snackbarDispatcher: SnackbarDispatcher,
    observeAliasOptions: ObserveAliasOptions,
    observeVaults: ObserveVaultsWithItemCount,
    observeUpgradeInfo: ObserveUpgradeInfo,
    savedStateHandle: SavedStateHandle,
    telemetryManager: TelemetryManager,
    draftRepository: DraftRepository
) : CreateAliasViewModel(
    accountManager,
    createAlias,
    snackbarDispatcher,
    telemetryManager,
    draftRepository,
    observeUpgradeInfo,
    observeAliasOptions,
    observeVaults,
    savedStateHandle
) {

    private val isEditMode = savedStateHandle.get<Boolean>(IsEditAliasNavArg.key) ?: false

    init {
        isDraft = true
    }

    fun setInitialState(title: String) = viewModelScope.launch {
        if (isEditMode) {
            resetWithDraft()
        } else {
            resetWithTitle(title)
        }
    }

    fun resetAliasDraftSavedState() {
        isAliasDraftSavedState.update { AliasDraftSavedState.Unknown }
    }

    private suspend fun resetWithDraft() {
        val draft = draftRepository.get<AliasItem>(KEY_DRAFT_ALIAS).firstOrNull()
        if (draft == null || draft.isEmpty()) {
            resetWithTitle("")
            return
        }

        val draftContent = draft as Some<AliasItem>
        aliasItemState.update { draftContent.value }
    }

    private fun resetWithTitle(title: String) {
        val draft = draftRepository.delete<AliasItem>(KEY_DRAFT_ALIAS)
        when (draft) {
            is Some -> {
                aliasItemState.update { draft.value }
            }

            else -> {
                if (aliasItemState.value.prefix.isBlank()) {
                    if (title.isBlank()) {
                        onPrefixChange(randomPrefix())
                    } else {
                        titlePrefixInSync = true
                        onTitleChange(title)
                    }
                }
            }
        }
    }

    private fun randomPrefix(): String {
        val dict = "abcdefghijklmnopqrstuvwxyz0123456789"
        var res = ""
        while (res.length < PREFIX_LENGTH) {
            res += dict.random()
        }
        return res
    }

    companion object {
        private const val PREFIX_LENGTH = 6
    }
}
