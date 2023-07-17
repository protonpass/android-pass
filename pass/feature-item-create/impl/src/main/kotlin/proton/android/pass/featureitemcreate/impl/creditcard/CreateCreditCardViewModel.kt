package proton.android.pass.featureitemcreate.impl.creditcard

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState.NotLoading
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.featureitemcreate.impl.ItemCreate
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.common.ShareError
import proton.android.pass.featureitemcreate.impl.common.ShareUiState
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardSnackbarMessage.ItemCreated
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardSnackbarMessage.ItemCreationError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.IncItemCreatedCount
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.pass.domain.ShareId
import proton.pass.domain.VaultWithItemCount
import javax.inject.Inject

@HiltViewModel
class CreateCreditCardViewModel @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val createItem: CreateItem,
    private val accountManager: AccountManager,
    private val telemetryManager: TelemetryManager,
    private val incItemCreatedCount: IncItemCreatedCount,
    observeVaults: ObserveVaultsWithItemCount,
    savedStateHandle: SavedStateHandleProvider,
    canPerformPaidAction: CanPerformPaidAction,
) : BaseCreditCardViewModel(
    encryptionContextProvider = encryptionContextProvider,
    canPerformPaidAction = canPerformPaidAction
) {

    private val navShareId: Option<ShareId> =
        savedStateHandle.get().get<String>(CommonOptionalNavArgId.ShareId.key)
            .toOption()
            .map { ShareId(it) }
    private val navShareIdState: MutableStateFlow<Option<ShareId>> = MutableStateFlow(navShareId)

    private val selectedShareIdState: MutableStateFlow<Option<ShareId>> = MutableStateFlow(None)
    private val observeAllVaultsFlow: Flow<List<VaultWithItemCount>> =
        observeVaults().distinctUntilChanged()

    private val shareUiState: StateFlow<ShareUiState> = combine(
        navShareIdState,
        selectedShareIdState,
        observeAllVaultsFlow.asLoadingResult(),
        canPerformPaidAction().asLoadingResult()
    ) { navShareId, selectedShareId, allSharesResult, canDoPaidAction ->
        val allShares = when (allSharesResult) {
            is LoadingResult.Error -> return@combine ShareUiState.Error(ShareError.SharesNotAvailable)
            LoadingResult.Loading -> return@combine ShareUiState.Loading
            is LoadingResult.Success -> allSharesResult.data
        }
        val canSwitchVaults = when (canDoPaidAction) {
            is LoadingResult.Error -> return@combine ShareUiState.Error(ShareError.UpgradeInfoNotAvailable)
            LoadingResult.Loading -> return@combine ShareUiState.Loading
            is LoadingResult.Success -> canDoPaidAction.data
        }

        if (allShares.isEmpty()) {
            return@combine ShareUiState.Error(ShareError.EmptyShareList)
        }
        val selectedVault = if (!canSwitchVaults) {
            val primaryVault = allShares.firstOrNull { it.vault.isPrimary }
            if (primaryVault == null) {
                PassLogger.w(TAG, "No primary vault found")
                return@combine ShareUiState.Error(ShareError.NoPrimaryVault)
            }
            primaryVault
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

    val state: StateFlow<CreateCreditCardUiState> = combine(
        shareUiState,
        baseState
    ) { shareUiState, baseState ->
        CreateCreditCardUiState.Success(
            shareUiState = shareUiState,
            baseState = baseState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CreateCreditCardUiState.NotInitialised
    )

    fun changeVault(shareId: ShareId) {
        onUserEditedContent()
        selectedShareIdState.update { shareId.toOption() }
    }

    fun createItem() = viewModelScope.launch {
        val shouldCreate = validateItem()
        if (!shouldCreate) return@launch

        isLoadingState.update { IsLoadingState.Loading }
        val vault = when (val state = shareUiState.value) {
            is ShareUiState.Error -> null
            ShareUiState.Loading -> null
            ShareUiState.NotInitialised -> null
            is ShareUiState.Success -> state.currentVault
        }
        val userId = accountManager.getPrimaryUserId()
            .firstOrNull { userId -> userId != null }
        if (userId != null && vault != null) {
            runCatching {
                createItem(
                    userId = userId,
                    shareId = vault.vault.shareId,
                    itemContents = itemContentState.value
                )
            }.onSuccess { item ->
                incItemCreatedCount()
                isItemSavedState.update {
                    encryptionContextProvider.withEncryptionContext {
                        ItemSavedState.Success(
                            item.id,
                            item.toUiModel(this@withEncryptionContext)
                        )
                    }
                }
                telemetryManager.sendEvent(ItemCreate(EventItemType.CreditCard))
                snackbarDispatcher(ItemCreated)
            }.onFailure {
                PassLogger.e(TAG, it, "Could not create item")
                snackbarDispatcher(ItemCreationError)
            }
        } else {
            snackbarDispatcher(ItemCreationError)
        }
        isLoadingState.update { NotLoading }
    }

    companion object {
        private const val TAG = "CreateCreditCardViewModel"
    }
}
