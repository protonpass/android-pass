package proton.android.pass.featureitemcreate.impl.note

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.featureitemcreate.impl.ItemCreate
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.login.ShareError
import proton.android.pass.featureitemcreate.impl.login.ShareUiState
import proton.android.pass.featureitemcreate.impl.note.NoteSnackbarMessage.ItemCreationError
import proton.android.pass.featureitemcreate.impl.note.NoteSnackbarMessage.NoteCreated
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.pass.domain.ShareId
import proton.pass.domain.VaultWithItemCount
import javax.inject.Inject

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val getShare: GetShareById,
    private val itemRepository: ItemRepository,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val telemetryManager: TelemetryManager,
    observeVaults: ObserveVaultsWithItemCount,
    observeUpgradeInfo: ObserveUpgradeInfo,
    savedStateHandle: SavedStateHandle
) : BaseNoteViewModel(snackbarDispatcher) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val navShareId: Option<ShareId> =
        savedStateHandle.get<String>(CommonOptionalNavArgId.ShareId.key)
            .toOption()
            .map { ShareId(it) }
    private val navShareIdState: MutableStateFlow<Option<ShareId>> = MutableStateFlow(navShareId)

    private val selectedShareIdState: MutableStateFlow<Option<ShareId>> = MutableStateFlow(None)
    private val observeAllVaultsFlow: Flow<List<VaultWithItemCount>> =
        observeVaults().distinctUntilChanged()
    private val upgradeInfoFlow: Flow<UpgradeInfo> = observeUpgradeInfo().distinctUntilChanged()

    private val shareUiState: StateFlow<ShareUiState> = combine(
        navShareIdState,
        selectedShareIdState,
        observeAllVaultsFlow.asLoadingResult(),
        upgradeInfoFlow.asLoadingResult()
    ) { navShareId, selectedShareId, allSharesResult, upgradeInfoResult ->
        val allShares = when (allSharesResult) {
            is LoadingResult.Error -> return@combine ShareUiState.Error(ShareError.SharesNotAvailable)
            LoadingResult.Loading -> return@combine ShareUiState.Loading
            is LoadingResult.Success -> allSharesResult.data
        }
        val upgradeInfo = when (upgradeInfoResult) {
            is LoadingResult.Error -> return@combine ShareUiState.Error(ShareError.UpgradeInfoNotAvailable)
            LoadingResult.Loading -> return@combine ShareUiState.Loading
            is LoadingResult.Success -> upgradeInfoResult.data
        }

        if (allShares.isEmpty()) {
            return@combine ShareUiState.Error(ShareError.EmptyShareList)
        }
        val selectedVault = if (upgradeInfo.isUpgradeAvailable) {
            allShares.firstOrNull { it.vault.isPrimary }
                ?: return@combine ShareUiState.Error(ShareError.NoPrimaryVault)
        } else {
            allShares
                .firstOrNull { it.vault.shareId == selectedShareId.value() }
                ?: allShares.firstOrNull { it.vault.shareId == navShareId.value() }
                ?: allShares.firstOrNull { it.vault.isPrimary }
                ?: allShares.firstOrNull()
                ?: return@combine ShareUiState.Error(ShareError.EmptyShareList)
        }
        ShareUiState.Success(
            vaultList = allShares,
            currentVault = selectedVault
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShareUiState.NotInitialised
    )

    val createNoteUiState: StateFlow<CreateNoteUiState> = combine(
        shareUiState,
        baseNoteUiState,
        ::CreateNoteUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CreateNoteUiState.Initial
    )

    fun createNote(shareId: ShareId) = viewModelScope.launch(coroutineExceptionHandler) {
        val noteItem = noteItemState.value
        val noteItemValidationErrors = noteItem.validate()
        if (noteItemValidationErrors.isNotEmpty()) {
            noteItemValidationErrorsState.update { noteItemValidationErrors }
        } else {
            isLoadingState.update { IsLoadingState.Loading }
            val userId = accountManager.getPrimaryUserId()
                .first { userId -> userId != null }
            if (userId != null) {
                runCatching { getShare(userId, shareId) }
                    .onFailure { PassLogger.e(TAG, it, "Error getting share") }
                    .mapCatching { share ->
                        val itemContents = noteItem.toItemContents()
                        itemRepository.createItem(userId, share, itemContents)
                    }
                    .onFailure {
                        PassLogger.e(TAG, it, "Create item error")
                        snackbarDispatcher(ItemCreationError)
                    }
                    .map { item ->
                        isItemSavedState.update {
                            encryptionContextProvider.withEncryptionContext {
                                ItemSavedState.Success(
                                    item.id,
                                    item.toUiModel(this@withEncryptionContext)
                                )
                            }
                        }
                        snackbarDispatcher(NoteCreated)
                        telemetryManager.sendEvent(ItemCreate(EventItemType.Note))
                    }
            } else {
                PassLogger.i(TAG, "Empty User Id")
                snackbarDispatcher(ItemCreationError)
            }
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    fun changeVault(shareId: ShareId) = viewModelScope.launch {
        onUserEditedContent()
        selectedShareIdState.update { shareId.toOption() }
    }

    companion object {
        private const val TAG = "CreateNoteViewModel"
    }
}
