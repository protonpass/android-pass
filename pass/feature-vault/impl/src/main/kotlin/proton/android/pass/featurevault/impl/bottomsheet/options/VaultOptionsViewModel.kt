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
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
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
    observeUpgradeInfo: ObserveUpgradeInfo,
    observeVaults: ObserveVaults,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navShareId: ShareId =
        ShareId(requireNotNull(savedStateHandle.get<String>(CommonNavArgId.ShareId.key)))

    val state: StateFlow<VaultOptionsUiState> = combine(
        observeVaults().asLoadingResult(),
        observeUpgradeInfo().asLoadingResult()
    ) { vaultResult, upgradeInfoResult ->
        val vaultList = when (vaultResult) {
            is LoadingResult.Error -> return@combine run {
                snackbarDispatcher(CannotGetVaultListError)
                PassLogger.w(TAG, vaultResult.exception, "Cannot get vault list")
                VaultOptionsUiState.Error
            }
            LoadingResult.Loading -> return@combine VaultOptionsUiState.Loading
            is LoadingResult.Success -> vaultResult.data
        }
        val upgradeInfo = when (upgradeInfoResult) {
            is LoadingResult.Error -> return@combine run {
                snackbarDispatcher(CannotGetVaultUpgradeInfoError)
                PassLogger.w(TAG, upgradeInfoResult.exception, "Cannot get upgrade info")
                VaultOptionsUiState.Error
            }
            LoadingResult.Loading -> return@combine VaultOptionsUiState.Loading
            is LoadingResult.Success -> upgradeInfoResult.data
        }
        val selectedVault = vaultList.firstOrNull { it.shareId == navShareId }
            ?: return@combine VaultOptionsUiState.Error
        val canUpgrade = upgradeInfo.isUpgradeAvailable
        val canEdit = !canUpgrade || selectedVault.isPrimary
        val canMigrate = if (canUpgrade) {
            vaultList.size > 1 && !selectedVault.isPrimary
        } else {
            vaultList.size > 1
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
