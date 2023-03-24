package proton.android.pass.featurehome.impl.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.logError
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.commonuimodels.api.ShareUiModelWithItemCount
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.errors.CannotDeleteCurrentVaultError
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.DeleteVault
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.featurehome.impl.HomeSnackbarMessage
import proton.android.pass.featurehome.impl.HomeVaultSelection
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class VaultDrawerViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUser,
    observeVaults: ObserveVaultsWithItemCount,
    itemRepository: ItemRepository,
    private val deleteVault: DeleteVault,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val currentUserFlow = observeCurrentUser().filterNotNull()
    private val vaultSelectionState =
        MutableStateFlow<HomeVaultSelection>(HomeVaultSelection.AllVaults)

    data class ShareUiModelsWithTrashedCount(
        val models: List<ShareUiModelWithItemCount>,
        val trashedCount: Long
    )

    private val allShareUiModelFlow: Flow<ShareUiModelsWithTrashedCount> = observeVaults()
        .map { shares ->
            when (shares) {
                LoadingResult.Loading -> ShareUiModelsWithTrashedCount(emptyList(), 0)
                is LoadingResult.Error -> {
                    PassLogger.e(TAG, shares.exception, "Cannot retrieve all shares")
                    ShareUiModelsWithTrashedCount(emptyList(), 0)
                }
                is LoadingResult.Success -> {
                    var totalTrashed = 0L
                    val res = shares.data
                        .map {
                            totalTrashed += it.trashedItemCount
                            ShareUiModelWithItemCount(
                                id = it.vault.shareId,
                                name = it.vault.name,
                                activeItemCount = it.activeItemCount,
                                trashedItemCount = it.trashedItemCount,
                                color = it.vault.color,
                                icon = it.vault.icon
                            )
                        }

                    ShareUiModelsWithTrashedCount(
                        models = res,
                        trashedCount = totalTrashed
                    )
                }
            }
        }
        .distinctUntilChanged()

    private val itemCountSummaryFlow = combine(
        currentUserFlow,
        vaultSelectionState,
        allShareUiModelFlow
    ) { user, selectedVault, allShares ->
        user to when (selectedVault) {
            is HomeVaultSelection.Vault -> allShares.models.filter { share -> share.id == selectedVault.shareId }
            HomeVaultSelection.AllVaults -> allShares.models
            HomeVaultSelection.Trash -> allShares.models // handle trash state
        }
    }
        .flatMapLatest { pair ->
            itemRepository.observeItemCountSummary(pair.first.userId, pair.second.map { it.id })
        }

    val drawerUiState = combine(
        itemCountSummaryFlow,
        allShareUiModelFlow,
        vaultSelectionState
    ) { itemCountSummary, shares, selectedVault ->
        VaultDrawerUiState(
            itemCountSummary = itemCountSummary,
            vaultSelection = selectedVault,
            shares = shares.models.toImmutableList(),
            totalTrashedItems = shares.trashedCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VaultDrawerUiState(
            ItemCountSummary.Initial,
            HomeVaultSelection.AllVaults,
            persistentListOf(),
            0
        )
    )

    fun setVaultSelection(homeVaultSelection: HomeVaultSelection) = viewModelScope.launch {
        vaultSelectionState.update { homeVaultSelection }
    }

    fun onDeleteVault(shareId: ShareId) = viewModelScope.launch {
        deleteVault(shareId)
            .onSuccess {
                snackbarDispatcher(HomeSnackbarMessage.DeleteVaultSuccess)
            }
            .onError {
                when (it) {
                    is CannotDeleteCurrentVaultError ->
                        snackbarDispatcher(HomeSnackbarMessage.CannotDeleteCurrentVault)
                    else -> snackbarDispatcher(HomeSnackbarMessage.DeleteVaultError)
                }
            }
            .logError(PassLogger, TAG, "Delete Vault Failed")
    }

    companion object {
        private const val TAG = "VaultDrawerViewModel"
    }
}
