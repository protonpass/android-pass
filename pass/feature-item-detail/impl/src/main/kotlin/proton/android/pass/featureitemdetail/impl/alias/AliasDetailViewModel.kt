package proton.android.pass.featureitemdetail.impl.alias

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsPermanentlyDeletedState
import proton.android.pass.composecomponents.impl.uievents.IsRestoredFromTrashState
import proton.android.pass.composecomponents.impl.uievents.IsSentToTrashState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.DeleteItem
import proton.android.pass.data.api.usecases.GetAliasDetails
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.RestoreItem
import proton.android.pass.data.api.usecases.TrashItem
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.AliasCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.InitError
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemMovedToTrash
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemNotMovedToTrash
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemNotRestored
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemRestored
import proton.android.pass.featureitemdetail.impl.ItemDelete
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId
import javax.inject.Inject
import proton.android.pass.common.api.combine as combineN

@HiltViewModel
class AliasDetailViewModel @Inject constructor(
    private val clipboardManager: ClipboardManager,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val trashItem: TrashItem,
    private val deleteItem: DeleteItem,
    private val restoreItem: RestoreItem,
    private val telemetryManager: TelemetryManager,
    getItemById: GetItemById,
    getAliasDetails: GetAliasDetails,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val shareId: ShareId =
        ShareId(requireNotNull(savedStateHandle.get<String>(CommonNavArgId.ShareId.key)))
    private val itemId: ItemId =
        ItemId(requireNotNull(savedStateHandle.get<String>(CommonNavArgId.ItemId.key)))

    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val isItemSentToTrashState: MutableStateFlow<IsSentToTrashState> =
        MutableStateFlow(IsSentToTrashState.NotSent)
    private val isPermanentlyDeletedState: MutableStateFlow<IsPermanentlyDeletedState> =
        MutableStateFlow(IsPermanentlyDeletedState.NotDeleted)
    private val isRestoredFromTrashState: MutableStateFlow<IsRestoredFromTrashState> =
        MutableStateFlow(IsRestoredFromTrashState.NotRestored)

    val uiState: StateFlow<AliasDetailUiState> = combineN(
        getItemById(shareId, itemId),
        getAliasDetails(shareId, itemId).asLoadingResult(),
        isLoadingState,
        isItemSentToTrashState,
        isPermanentlyDeletedState,
        isRestoredFromTrashState
    ) { itemLoadingResult,
        aliasDetailsResult,
        isLoading,
        isItemSentToTrash,
        isPermanentlyDeleted,
        isRestoredFromTrash ->
        when (itemLoadingResult) {
            is LoadingResult.Error -> {
                snackbarDispatcher(InitError)
                AliasDetailUiState.Error
            }
            LoadingResult.Loading -> AliasDetailUiState.NotInitialised
            is LoadingResult.Success -> {
                encryptionContextProvider.withEncryptionContext {
                    AliasDetailUiState.Success(
                        itemUiModel = itemLoadingResult.data.toUiModel(this),
                        mailboxes = aliasDetailsResult.getOrNull()?.mailboxes?.toPersistentList()
                            ?: persistentListOf(),
                        isLoading = aliasDetailsResult is LoadingResult.Loading || isLoading.value(),
                        isLoadingMailboxes = aliasDetailsResult is LoadingResult.Loading,
                        isItemSentToTrash = isItemSentToTrash.value(),
                        isPermanentlyDeleted = isPermanentlyDeleted.value(),
                        isRestoredFromTrash = isRestoredFromTrash.value()
                    )
                }
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = AliasDetailUiState.NotInitialised
        )

    fun onCopyAlias(alias: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                clipboardManager.copyToClipboard(alias)
            }
            snackbarDispatcher(AliasCopiedToClipboard)
        }
    }

    fun onMoveToTrash(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        trashItem(shareId = shareId, itemId = itemId)
            .onSuccess {
                isItemSentToTrashState.update { IsSentToTrashState.Sent }
                snackbarDispatcher(ItemMovedToTrash)
            }
            .onError {
                snackbarDispatcher(ItemNotMovedToTrash)
                PassLogger.d(TAG, it, "Could not delete item")
            }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun onPermanentlyDelete(shareId: ShareId, itemId: ItemId, itemType: ItemType) =
        viewModelScope.launch {
            isLoadingState.update { IsLoadingState.Loading }
            runCatching {
                deleteItem(shareId = shareId, itemId = itemId)
            }.onSuccess {
                telemetryManager.sendEvent(ItemDelete(EventItemType.from(itemType)))
                isPermanentlyDeletedState.update { IsPermanentlyDeletedState.Deleted }
                snackbarDispatcher(DetailSnackbarMessages.ItemPermanentlyDeleted)
                PassLogger.i(TAG, "Item deleted successfully")
            }.onFailure {
                snackbarDispatcher(DetailSnackbarMessages.ItemNotPermanentlyDeleted)
                PassLogger.i(TAG, it, "Could not delete item")
            }
            isLoadingState.update { IsLoadingState.NotLoading }
        }

    fun onItemRestore(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        runCatching {
            restoreItem(shareId = shareId, itemId = itemId)
        }.onSuccess {
            isRestoredFromTrashState.update { IsRestoredFromTrashState.Restored }
            PassLogger.i(TAG, "Item restored successfully")
            snackbarDispatcher(ItemRestored)
        }.onFailure {
            PassLogger.i(TAG, it, "Error restoring item")
            snackbarDispatcher(ItemNotRestored)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    companion object {
        private const val TAG = "AliasDetailViewModel"
    }
}
