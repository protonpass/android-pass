package proton.android.pass.featuremigrate.impl.confirmvault

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.android.pass.data.api.usecases.MigrateItem
import proton.android.pass.data.api.usecases.MigrateVault
import proton.android.pass.featuremigrate.impl.MigrateModeArg
import proton.android.pass.featuremigrate.impl.MigrateModeValue
import proton.android.pass.featuremigrate.impl.MigrateSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.DestinationShareNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class MigrateConfirmVaultViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val migrateItem: MigrateItem,
    private val migrateVault: MigrateVault,
    private val snackbarDispatcher: SnackbarDispatcher,
    getVaultById: GetVaultWithItemCountById
) : ViewModel() {

    private val mode = getMode()

    private val isLoadingFlow: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val eventFlow: MutableStateFlow<Option<ConfirmMigrateEvent>> =
        MutableStateFlow(None)

    private val getVaultFlow = getVaultById(shareId = mode.destShareId)
        .catch {
            PassLogger.e(TAG, it, "Error getting Vault by id")
            eventFlow.update { ConfirmMigrateEvent.Close.toOption() }
        }
        .asLoadingResult()

    val state: StateFlow<MigrateConfirmVaultUiState> = combine(
        isLoadingFlow,
        getVaultFlow,
        eventFlow
    ) { isLoading, vaultRes, event ->
        val loading = isLoading is IsLoadingState.Loading || vaultRes is LoadingResult.Loading
        val vault = vaultRes.getOrNull().toOption()
        MigrateConfirmVaultUiState(
            isLoading = IsLoadingState.from(loading),
            vault = vault,
            event = event,
            mode = mode.migrateMode()
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MigrateConfirmVaultUiState.Initial(mode.migrateMode())
    )

    fun onConfirm() = viewModelScope.launch {
        when (mode) {
            is Mode.MigrateAllItems -> performAllItemsMigration(
                sourceShareId = mode.sourceShareId,
                destShareId = mode.destShareId
            )
            is Mode.MigrateItem -> performItemMigration(
                sourceShareId = mode.sourceShareId,
                destShareId = mode.destShareId,
                itemId = mode.itemId
            )
        }
    }

    private suspend fun performAllItemsMigration(
        sourceShareId: ShareId,
        destShareId: ShareId,
    ) {
        isLoadingFlow.update { IsLoadingState.Loading }
        runCatching {
            migrateVault(
                origin = sourceShareId,
                dest = destShareId
            )
        }.onSuccess {
            eventFlow.update { ConfirmMigrateEvent.AllItemsMigrated.some() }
            snackbarDispatcher(MigrateSnackbarMessage.VaultItemsMigrated)
            isLoadingFlow.update { IsLoadingState.NotLoading }
        }.onFailure {
            isLoadingFlow.update { IsLoadingState.NotLoading }
            PassLogger.e(TAG, it, "Error migrating all items")
            snackbarDispatcher(MigrateSnackbarMessage.VaultItemsNotMigrated)
        }
    }

    private suspend fun performItemMigration(
        sourceShareId: ShareId,
        destShareId: ShareId,
        itemId: ItemId
    ) {
        isLoadingFlow.update { IsLoadingState.Loading }
        runCatching {
            migrateItem(
                sourceShare = sourceShareId,
                itemId = itemId,
                destinationShare = destShareId
            )
        }.onSuccess { item ->
            eventFlow.update { ConfirmMigrateEvent.ItemMigrated(item.shareId, item.id).toOption() }
            snackbarDispatcher(MigrateSnackbarMessage.ItemMigrated)
            isLoadingFlow.update { IsLoadingState.NotLoading }
        }.onFailure {
            isLoadingFlow.update { IsLoadingState.NotLoading }
            PassLogger.e(TAG, it, "Error migrating item")
            snackbarDispatcher(MigrateSnackbarMessage.ItemNotMigrated)
        }
    }

    fun onCancel() {
        eventFlow.update { ConfirmMigrateEvent.Close.toOption() }
    }

    private fun getMode(): Mode {
        val sourceShareId = getSourceShareId()
        val destShareId = getDestinationShareId()
        return when (getNavMode()) {
            MigrateModeValue.SingleItem -> Mode.MigrateItem(
                sourceShareId = sourceShareId,
                destShareId = destShareId,
                itemId = getItemId()
            )

            MigrateModeValue.AllVaultItems -> Mode.MigrateAllItems(
                sourceShareId = sourceShareId,
                destShareId = destShareId
            )
        }
    }

    internal sealed interface Mode {
        val sourceShareId: ShareId
        val destShareId: ShareId

        data class MigrateItem(
            override val sourceShareId: ShareId,
            override val destShareId: ShareId,
            val itemId: ItemId
        ) : Mode

        data class MigrateAllItems(
            override val sourceShareId: ShareId,
            override val destShareId: ShareId
        ) : Mode

        fun migrateMode(): MigrateMode = when (this) {
            is MigrateItem -> MigrateMode.MigrateItem
            is MigrateAllItems -> MigrateMode.MigrateAll
        }
    }

    private fun getNavMode(): MigrateModeValue =
        MigrateModeValue.valueOf(getNavArg(MigrateModeArg.key))

    private fun getSourceShareId(): ShareId = ShareId(getNavArg(CommonNavArgId.ShareId.key))
    private fun getDestinationShareId(): ShareId = ShareId(getNavArg(DestinationShareNavArgId.key))
    private fun getItemId(): ItemId = ItemId(getNavArg(CommonOptionalNavArgId.ItemId.key))

    private fun getNavArg(name: String): String =
        savedStateHandle.get<String>(name)
            ?: throw IllegalStateException("Missing $name nav argument")

    companion object {
        private const val TAG = "MigrateConfirmVaultViewModel"
    }

}
