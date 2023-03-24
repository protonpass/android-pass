package proton.android.pass.featureitemcreate.impl.alias.bottomsheet

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.ObserveAliasOptions
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.featureitemcreate.impl.alias.AliasDraftSavedState
import proton.android.pass.featureitemcreate.impl.alias.AliasItem
import proton.android.pass.featureitemcreate.impl.alias.CreateAliasViewModel
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
    savedStateHandle: SavedStateHandle,
    telemetryManager: TelemetryManager
) : CreateAliasViewModel(
    accountManager,
    createAlias,
    snackbarDispatcher,
    telemetryManager,
    observeAliasOptions,
    observeVaults,
    savedStateHandle
) {

    init {
        isDraft = true
    }

    fun setInitialState(title: String, aliasItem: AliasItem?) {
        if (aliasItem != null) {
            aliasItemState.update { aliasItem }
        } else {
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

    fun resetAliasDraftSavedState() {
        isAliasDraftSavedState.update { AliasDraftSavedState.Unknown }
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
