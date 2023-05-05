package proton.android.pass.featuremigrate.impl.selectvault

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
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.featuremigrate.impl.MigrateModeArg
import proton.android.pass.featuremigrate.impl.MigrateModeValue
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class MigrateSelectVaultViewModel @Inject constructor(
    observeVaults: ObserveVaultsWithItemCount,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val mode = getMode()

    private val eventFlow: MutableStateFlow<Option<SelectVaultEvent>> = MutableStateFlow(None)

    val state: StateFlow<MigrateSelectVaultUiState> = combine(
        observeVaults().asLoadingResult(),
        eventFlow
    ) { vaultResult, event ->
        when (val res = vaultResult) {
            LoadingResult.Loading -> MigrateSelectVaultUiState.Initial
            is LoadingResult.Error -> {
                PassLogger.e(TAG, res.exception, "Error observing active vaults")
                MigrateSelectVaultUiState(
                    vaultList = persistentListOf(),
                    event = SelectVaultEvent.Close.toOption(),
                    mode = mode.migrateMode()
                )
            }
            is LoadingResult.Success -> {
                val vaultEnabledPairs = res.data.map {
                    VaultEnabledPair(
                        vault = it,
                        isEnabled = it.vault.shareId != mode.shareId
                    )
                }
                MigrateSelectVaultUiState(
                    vaultList = vaultEnabledPairs.toImmutableList(),
                    event = event,
                    mode = mode.migrateMode()
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MigrateSelectVaultUiState.Initial
    )

    fun onVaultSelected(shareId: ShareId) {
        val event = when (mode) {
            is Mode.MigrateItem -> SelectVaultEvent.VaultSelectedForMigrateItem(
                sourceShareId = mode.shareId,
                itemId = mode.itemId,
                destinationShareId = shareId
            )
            is Mode.MigrateAllItems -> SelectVaultEvent.VaultSelectedForMigrateAll(
                sourceShareId = mode.shareId,
                destinationShareId = shareId
            )
        }

        eventFlow.update { event.toOption() }
    }

    fun clearEvent() {
        eventFlow.update { None }
    }

    private fun getMode(): Mode {
        val sourceShareId = getSourceShareId()
        return when (getNavMode()) {
            MigrateModeValue.SingleItem -> Mode.MigrateItem(sourceShareId, getItemId())
            MigrateModeValue.AllVaultItems -> Mode.MigrateAllItems(sourceShareId)
        }
    }

    private fun getNavMode(): MigrateModeValue = MigrateModeValue.valueOf(getNavArg(MigrateModeArg.key))
    private fun getSourceShareId(): ShareId = ShareId(getNavArg(CommonNavArgId.ShareId.key))
    private fun getItemId(): ItemId = ItemId(getNavArg(CommonOptionalNavArgId.ItemId.key))

    private fun getNavArg(name: String): String =
        savedStateHandle.get<String>(name)
            ?: throw IllegalStateException("Missing $name nav argument")

    internal sealed interface Mode {

        val shareId: ShareId

        data class MigrateItem(
            override val shareId: ShareId,
            val itemId: ItemId
        ) : Mode

        data class MigrateAllItems(override val shareId: ShareId) : Mode

        fun migrateMode(): MigrateMode = when (this) {
            is MigrateItem -> MigrateMode.MigrateItem
            is MigrateAllItems -> MigrateMode.MigrateAll
        }
    }

    companion object {
        private const val TAG = "MigrateSelectVaultViewModel"
    }
}
