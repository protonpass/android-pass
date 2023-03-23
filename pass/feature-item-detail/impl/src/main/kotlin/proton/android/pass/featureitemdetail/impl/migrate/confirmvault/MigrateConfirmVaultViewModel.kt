package proton.android.pass.featureitemdetail.impl.migrate.confirmvault

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.GetVaultWithItemCountById
import proton.android.pass.data.api.usecases.MigrateItem
import proton.android.pass.featureitemdetail.impl.migrate.ItemMigrateSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.DestinationShareNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.VaultWithItemCount
import javax.inject.Inject

@HiltViewModel
class MigrateConfirmVaultViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val migrateItem: MigrateItem,
    private val snackbarDispatcher: SnackbarDispatcher,
    getVaultById: GetVaultWithItemCountById
) : ViewModel() {

    private val sourceShareId = getSourceShareId()
    private val destinationShareId = getDestinationShareId()
    private val itemId = getItemId()

    private val isLoadingFlow: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.Loading)
    private val eventFlow: MutableStateFlow<Option<ConfirmMigrateEvent>> =
        MutableStateFlow(None)

    private val getVaultFlow: Flow<Option<VaultWithItemCount>> =
        getVaultById(shareId = destinationShareId)
            .map {
                isLoadingFlow.update { IsLoadingState.NotLoading }
                it.toOption()
            }
            .onStart { emit(None) }
            .catch {
                PassLogger.e(TAG, it, "Error getting Vault by id")
                eventFlow.update { ConfirmMigrateEvent.Close.toOption() }
            }

    val state: StateFlow<MigrateConfirmVaultUiState> = combine(
        isLoadingFlow,
        getVaultFlow,
        eventFlow
    ) { isLoading, vault, event ->
        MigrateConfirmVaultUiState(
            isLoading = isLoading,
            vault = vault,
            event = event
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MigrateConfirmVaultUiState.Initial
    )

    fun onConfirm() = viewModelScope.launch {
        isLoadingFlow.update { IsLoadingState.Loading }

        runCatching {
            migrateItem(sourceShare = sourceShareId, itemId = itemId, destinationShare = destinationShareId)
        }.onSuccess { item ->
            isLoadingFlow.update { IsLoadingState.NotLoading }
            eventFlow.update { ConfirmMigrateEvent.Migrated(item.shareId, item.id).toOption() }
            snackbarDispatcher(ItemMigrateSnackbarMessage.ItemMigrated)
        }.onFailure {
            isLoadingFlow.update { IsLoadingState.NotLoading }
            PassLogger.e(TAG, it, "Error migrating item")
            snackbarDispatcher(ItemMigrateSnackbarMessage.ItemNotMigrated)
        }
    }

    fun onCancel() {
        eventFlow.update { ConfirmMigrateEvent.Close.toOption() }
    }

    private fun getSourceShareId(): ShareId = ShareId(getNavArg(CommonNavArgId.ShareId.key))
    private fun getDestinationShareId(): ShareId = ShareId(getNavArg(DestinationShareNavArgId.key))
    private fun getItemId(): ItemId = ItemId(getNavArg(CommonNavArgId.ItemId.key))

    private fun getNavArg(name: String): String =
        savedStateHandle.get<String>(name)
            ?: throw IllegalStateException("Missing $name nav argument")

    companion object {
        private const val TAG = "MigrateConfirmVaultViewModel"
    }

}
