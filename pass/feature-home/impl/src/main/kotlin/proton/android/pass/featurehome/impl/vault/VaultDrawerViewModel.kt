package proton.android.pass.featurehome.impl.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.commonuimodels.api.ShareUiModelWithItemCount
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.featurehome.impl.HomeVaultSelection
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

@HiltViewModel
class VaultDrawerViewModel @Inject constructor(
    observeVaultsWithItemCount: ObserveVaultsWithItemCount
) : ViewModel() {

    private val vaultSelectionState: MutableStateFlow<HomeVaultSelection> =
        MutableStateFlow(HomeVaultSelection.AllVaults)

    val drawerUiState: StateFlow<VaultDrawerUiState> = combine(
        observeVaultsWithItemCount().asLoadingResult(),
        vaultSelectionState
    ) { shares, selectedVault ->
        when (shares) {
            LoadingResult.Loading -> VaultDrawerUiState(
                vaultSelection = selectedVault,
                shares = persistentListOf(),
                totalTrashedItems = 0
            )

            is LoadingResult.Error -> {
                PassLogger.w(TAG, shares.exception, "Cannot retrieve all shares")
                VaultDrawerUiState(
                    vaultSelection = selectedVault,
                    shares = persistentListOf(),
                    totalTrashedItems = 0
                )
            }

            is LoadingResult.Success -> {
                val shareIdList = shares.data.map { it.vault.shareId }
                if (selectedVault is HomeVaultSelection.Vault && !shareIdList.contains(selectedVault.shareId)) {
                    vaultSelectionState.update { HomeVaultSelection.AllVaults }
                }
                val sharesWithCount = shares.data
                    .map {
                        ShareUiModelWithItemCount(
                            id = it.vault.shareId,
                            name = it.vault.name,
                            activeItemCount = it.activeItemCount,
                            trashedItemCount = it.trashedItemCount,
                            color = it.vault.color,
                            icon = it.vault.icon,
                            isPrimary = it.vault.isPrimary
                        )
                    }
                    .toImmutableList()
                val totalTrashed = shares.data.sumOf { it.trashedItemCount }
                VaultDrawerUiState(
                    vaultSelection = selectedVault,
                    shares = sharesWithCount,
                    totalTrashedItems = totalTrashed
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VaultDrawerUiState(
            HomeVaultSelection.AllVaults,
            persistentListOf(),
            0
        )
    )

    fun setVaultSelection(homeVaultSelection: HomeVaultSelection) = viewModelScope.launch {
        vaultSelectionState.update { homeVaultSelection }
    }

    companion object {
        private const val TAG = "VaultDrawerViewModel"
    }
}
