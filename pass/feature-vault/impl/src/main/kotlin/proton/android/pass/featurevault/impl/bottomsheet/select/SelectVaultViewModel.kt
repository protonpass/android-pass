package proton.android.pass.featurevault.impl.bottomsheet.select

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.featurevault.impl.VaultSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class SelectVaultViewModel @Inject constructor(
    observeVaultsWithItemCount: ObserveVaultsWithItemCount,
    snackbarDispatcher: SnackbarDispatcher,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val selected: ShareId =
        ShareId(requireNotNull(savedStateHandle.get<String>(SelectedVaultArg.key)))

    val state: StateFlow<SelectVaultUiState> = observeVaultsWithItemCount()
        .asLoadingResult()
        .map { vaultsResult ->
            when (vaultsResult) {
                LoadingResult.Loading -> SelectVaultUiState.Loading
                is LoadingResult.Success -> {
                    val shares = vaultsResult.data.map { it.vault.shareId }
                    if (shares.contains(selected)) {
                        SelectVaultUiState.Success(
                            vaults = vaultsResult.data.toPersistentList(),
                            selected = vaultsResult.data.first { it.vault.shareId == selected }
                        )
                    } else {
                        PassLogger.w(TAG, "Error finding current vault")
                        snackbarDispatcher(VaultSnackbarMessage.CannotFindVaultError)
                        SelectVaultUiState.Error
                    }
                }

                is LoadingResult.Error -> {
                    PassLogger.w(TAG, vaultsResult.exception, "Error observing vaults")
                    snackbarDispatcher(VaultSnackbarMessage.CannotGetVaultListError)
                    SelectVaultUiState.Error
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SelectVaultUiState.Uninitialised
        )

    companion object {
        private const val TAG = "SelectVaultViewModel"
    }
}
