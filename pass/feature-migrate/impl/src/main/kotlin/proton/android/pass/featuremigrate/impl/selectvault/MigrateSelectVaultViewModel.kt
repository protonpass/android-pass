package proton.android.pass.featuremigrate.impl.selectvault

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.featuremigrate.impl.MigrateModeArg
import proton.android.pass.featuremigrate.impl.MigrateModeValue
import proton.android.pass.featuremigrate.impl.MigrateSnackbarMessage.CouldNotInit
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class MigrateSelectVaultViewModel @Inject constructor(
    canPerformPaidAction: CanPerformPaidAction,
    observeVaults: ObserveVaultsWithItemCount,
    snackbarDispatcher: SnackbarDispatcher,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val mode: Mode = getMode()

    private val eventFlow: MutableStateFlow<Option<SelectVaultEvent>> = MutableStateFlow(None)

    val state: StateFlow<MigrateSelectVaultUiState> = combine(
        canPerformPaidAction().asLoadingResult(),
        observeVaults().asLoadingResult(),
        eventFlow
    ) { canPerformPaidActionResult, vaultResult, event ->
        val canPerformPaidActionValue = when (canPerformPaidActionResult) {
            is LoadingResult.Error -> {
                snackbarDispatcher(CouldNotInit)
                PassLogger.w(
                    TAG,
                    canPerformPaidActionResult.exception,
                    "Error observing CanPerformPaidAction"
                )
                return@combine MigrateSelectVaultUiState.Error
            }

            LoadingResult.Loading -> return@combine MigrateSelectVaultUiState.Loading
            is LoadingResult.Success -> canPerformPaidActionResult.data
        }
        when (vaultResult) {
            LoadingResult.Loading -> MigrateSelectVaultUiState.Loading
            is LoadingResult.Error -> {
                snackbarDispatcher(CouldNotInit)
                PassLogger.w(TAG, vaultResult.exception, "Error observing active vaults")
                MigrateSelectVaultUiState.Error
            }

            is LoadingResult.Success -> MigrateSelectVaultUiState.Success(
                vaultList = vaultResult.data
                    .map {
                        if (canPerformPaidActionValue) {
                            VaultEnabledPair(
                                vault = it,
                                isEnabled = it.vault.shareId != mode.shareId
                            )
                        } else {
                            VaultEnabledPair(
                                vault = it,
                                isEnabled = it.vault.isPrimary
                            )
                        }
                    }
                    .toImmutableList(),
                event = event,
                mode = mode.migrateMode()
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MigrateSelectVaultUiState.Uninitialised
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
        val sourceShareId = ShareId(savedStateHandle.require(CommonNavArgId.ShareId.key))
        return when (MigrateModeValue.valueOf(savedStateHandle.require(MigrateModeArg.key))) {
            MigrateModeValue.SingleItem -> Mode.MigrateItem(
                shareId = sourceShareId,
                itemId = ItemId(savedStateHandle.require(CommonOptionalNavArgId.ItemId.key))
            )

            MigrateModeValue.AllVaultItems -> Mode.MigrateAllItems(sourceShareId)
        }
    }

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
