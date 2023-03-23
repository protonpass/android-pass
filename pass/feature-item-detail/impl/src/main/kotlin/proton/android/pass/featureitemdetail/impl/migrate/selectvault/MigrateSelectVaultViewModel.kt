package proton.android.pass.featureitemdetail.impl.migrate.selectvault

import androidx.lifecycle.SavedStateHandle
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
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class MigrateSelectVaultViewModel @Inject constructor(
    observeVaults: ObserveVaultsWithItemCount,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sourceShareId = getSourceShareId()
    private val itemId = getItemId()

    private val eventFlow: MutableStateFlow<Option<SelectVaultEvent>> = MutableStateFlow(None)

    val state: StateFlow<MigrateSelectVaultUiState> = combine(
        observeVaults(),
        eventFlow
    ) { vaultResult, event ->
        when (val res = vaultResult) {
            LoadingResult.Loading -> MigrateSelectVaultUiState.Initial
            is LoadingResult.Error -> {
                PassLogger.e(TAG, res.exception, "Error observing active vaults")
                MigrateSelectVaultUiState(
                    vaultList = persistentListOf(),
                    event = SelectVaultEvent.Close.toOption()
                )
            }
            is LoadingResult.Success -> {
                val vaultEnabledPairs = res.data.map {
                    VaultEnabledPair(
                        vault = it,
                        isEnabled = it.vault.shareId != sourceShareId
                    )
                }
                MigrateSelectVaultUiState(
                    vaultList = vaultEnabledPairs.toImmutableList(),
                    event = event
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MigrateSelectVaultUiState.Initial
    )

    fun onVaultSelected(shareId: ShareId) {
        eventFlow.update {
            SelectVaultEvent.SelectedVault(
                sourceShareId = sourceShareId,
                itemId = itemId,
                destinationShareId = shareId
            ).toOption()
        }
    }

    fun clearEvent() {
        eventFlow.update { None }
    }

    private fun getSourceShareId(): ShareId = ShareId(getNavArg(CommonNavArgId.ShareId.key))
    private fun getItemId(): ItemId = ItemId(getNavArg(CommonNavArgId.ItemId.key))

    private fun getNavArg(name: String): String =
        savedStateHandle.get<String>(name)
            ?: throw IllegalStateException("Missing $name nav argument")

    companion object {
        private const val TAG = "MigrateSelectVaultViewModel"
    }
}
