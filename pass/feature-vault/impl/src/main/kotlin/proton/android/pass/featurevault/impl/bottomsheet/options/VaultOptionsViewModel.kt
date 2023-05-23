package proton.android.pass.featurevault.impl.bottomsheet.options

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.CannotGetVaultListError
import proton.android.pass.featurevault.impl.VaultSnackbarMessage.CannotGetVaultUpgradeInfoError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class VaultOptionsViewModel @Inject constructor(
    snackbarDispatcher: SnackbarDispatcher,
    canPerformPaidAction: CanPerformPaidAction,
    observeVaults: ObserveVaults,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navShareId: ShareId =
        ShareId(requireNotNull(savedStateHandle.get<String>(CommonNavArgId.ShareId.key)))

    val state: StateFlow<VaultOptionsUiState> = combine(
        observeVaults().asLoadingResult(),
        canPerformPaidAction().asLoadingResult()
    ) { vaultResult, canPerformPaidActionResult ->
        val vaultList = when (vaultResult) {
            is LoadingResult.Error -> return@combine run {
                snackbarDispatcher(CannotGetVaultListError)
                PassLogger.w(TAG, vaultResult.exception, "Cannot get vault list")
                VaultOptionsUiState.Error
            }

            LoadingResult.Loading -> return@combine VaultOptionsUiState.Loading
            is LoadingResult.Success -> vaultResult.data
        }
        val canPerformPaidActionValue = when (canPerformPaidActionResult) {
            is LoadingResult.Error -> return@combine run {
                snackbarDispatcher(CannotGetVaultUpgradeInfoError)
                PassLogger.w(
                    TAG,
                    canPerformPaidActionResult.exception,
                    "Cannot get CanPerformPaidAction"
                )
                VaultOptionsUiState.Error
            }

            LoadingResult.Loading -> return@combine VaultOptionsUiState.Loading
            is LoadingResult.Success -> canPerformPaidActionResult.data
        }
        val selectedVault = vaultList.firstOrNull { it.shareId == navShareId }
            ?: return@combine VaultOptionsUiState.Error
        val canEdit = canPerformPaidActionValue || selectedVault.isPrimary
        val canMigrate = if (canPerformPaidActionValue) {
            vaultList.size > 1
        } else {
            vaultList.size > 1 && !selectedVault.isPrimary
        }
        val canDelete = !selectedVault.isPrimary
        VaultOptionsUiState.Success(
            shareId = navShareId,
            showEdit = canEdit,
            showMigrate = canMigrate,
            showDelete = canDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = VaultOptionsUiState.Uninitialised
    )

    companion object {
        private const val TAG = "VaultOptionsViewModel"
    }
}
