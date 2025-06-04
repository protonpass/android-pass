package proton.android.pass.features.itemcreate.creditcard

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.toOption
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState.NotLoading
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.attachments.LinkAttachmentsToItem
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.itemcreate.ItemCreate
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.OptionShareIdSaver
import proton.android.pass.features.itemcreate.common.ShareUiState
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandler
import proton.android.pass.features.itemcreate.common.formprocessor.CreditCardItemFormProcessor
import proton.android.pass.features.itemcreate.common.getShareUiStateFlow
import proton.android.pass.features.itemcreate.creditcard.CreditCardSnackbarMessage.ItemCreated
import proton.android.pass.features.itemcreate.creditcard.CreditCardSnackbarMessage.ItemCreationError
import proton.android.pass.features.itemcreate.creditcard.CreditCardSnackbarMessage.ItemLinkAttachmentsError
import proton.android.pass.inappreview.api.InAppReviewTriggerMetrics
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class CreateCreditCardViewModel @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val createItem: CreateItem,
    private val accountManager: AccountManager,
    private val telemetryManager: TelemetryManager,
    private val inAppReviewTriggerMetrics: InAppReviewTriggerMetrics,
    private val linkAttachmentsToItem: LinkAttachmentsToItem,
    userPreferencesRepository: UserPreferencesRepository,
    attachmentsHandler: AttachmentsHandler,
    observeVaults: ObserveVaultsWithItemCount,
    canPerformPaidAction: CanPerformPaidAction,
    observeDefaultVault: ObserveDefaultVault,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    customFieldHandler: CustomFieldHandler,
    customFieldDraftRepository: CustomFieldDraftRepository,
    creditCardItemFormProcessor: CreditCardItemFormProcessor,
    savedStateHandleProvider: SavedStateHandleProvider
) : BaseCreditCardViewModel(
    userPreferencesRepository = userPreferencesRepository,
    attachmentsHandler = attachmentsHandler,
    encryptionContextProvider = encryptionContextProvider,
    canPerformPaidAction = canPerformPaidAction,
    featureFlagsRepository = featureFlagsRepository,
    customFieldHandler = customFieldHandler,
    customFieldDraftRepository = customFieldDraftRepository,
    creditCardItemFormProcessor = creditCardItemFormProcessor,
    savedStateHandleProvider = savedStateHandleProvider
) {
    private val navShareId: Option<ShareId> =
        savedStateHandleProvider.get().get<String>(CommonOptionalNavArgId.ShareId.key)
            .toOption()
            .map { ShareId(it) }

    @OptIn(SavedStateHandleSaveableApi::class)
    private var selectedShareIdMutableState: Option<ShareId> by savedStateHandleProvider.get()
        .saveable(stateSaver = OptionShareIdSaver) { mutableStateOf(None) }
    private val selectedShareIdState: Flow<Option<ShareId>> =
        snapshotFlow { selectedShareIdMutableState }
            .filterNotNull()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = None
            )

    private val observeAllVaultsFlow: Flow<List<VaultWithItemCount>> =
        observeVaults().distinctUntilChanged()

    private val shareUiState: StateFlow<ShareUiState> = getShareUiStateFlow(
        navShareIdState = flowOf(navShareId),
        selectedShareIdState = selectedShareIdState,
        observeAllVaultsFlow = observeAllVaultsFlow.asLoadingResult(),
        observeDefaultVaultFlow = observeDefaultVault().asLoadingResult(),
        viewModelScope = viewModelScope,
        tag = TAG
    )

    internal val state: StateFlow<CreateCreditCardUiState> = combine(
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
        selectedShareIdMutableState = Some(shareId)
    }

    fun createItem() = viewModelScope.launch {
        if (!isFormStateValid()) return@launch

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
                val sanitisedItemFormState = creditCardItemFormState.sanitise()
                createItem(
                    userId = userId,
                    shareId = vault.vault.shareId,
                    itemContents = sanitisedItemFormState.toItemContents()
                )
            }
                .onFailure {
                    PassLogger.w(TAG, "Could not create item")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(ItemCreationError)
                }
                .onSuccess { item ->
                    snackbarDispatcher(ItemCreated)
                    runCatching {
                        if (isFileAttachmentsEnabled()) {
                            linkAttachmentsToItem(item.shareId, item.id, item.revision)
                        }
                    }.onFailure {
                        PassLogger.w(TAG, "Link attachment error")
                        PassLogger.w(TAG, it)
                        snackbarDispatcher(ItemLinkAttachmentsError)
                    }
                    inAppReviewTriggerMetrics.incrementItemCreatedCount()
                    isItemSavedState.update {
                        encryptionContextProvider.withEncryptionContext {
                            ItemSavedState.Success(
                                item.id,
                                item.toUiModel(this@withEncryptionContext)
                            )
                        }
                    }
                    telemetryManager.sendEvent(ItemCreate(EventItemType.CreditCard))
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
