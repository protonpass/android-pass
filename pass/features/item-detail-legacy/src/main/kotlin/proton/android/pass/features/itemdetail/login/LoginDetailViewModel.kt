/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.itemdetail.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.network.domain.ApiException
import proton.android.pass.biometry.AuthOverrideState
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.flatMap
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.map
import proton.android.pass.common.api.toOption
import proton.android.pass.commonrust.api.PasswordScore
import proton.android.pass.commonrust.api.PasswordScorer
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.FileHandler
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsPermanentlyDeletedState
import proton.android.pass.composecomponents.impl.uievents.IsRestoredFromTrashState
import proton.android.pass.composecomponents.impl.uievents.IsSentToTrashState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.data.api.errors.ItemNotFoundError
import proton.android.pass.data.api.repositories.BulkMoveToVaultRepository
import proton.android.pass.data.api.usecases.CanDisplayTotp
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.DeleteItems
import proton.android.pass.data.api.usecases.GetItemActions
import proton.android.pass.data.api.usecases.GetItemByAliasEmail
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.data.api.usecases.ObserveItemByIdWithVault
import proton.android.pass.data.api.usecases.PinItem
import proton.android.pass.data.api.usecases.RestoreItems
import proton.android.pass.data.api.usecases.TrashItems
import proton.android.pass.data.api.usecases.UnpinItem
import proton.android.pass.data.api.usecases.attachments.DownloadAttachment
import proton.android.pass.data.api.usecases.attachments.ObserveDetailItemAttachments
import proton.android.pass.data.api.usecases.capabilities.CanShareShare
import proton.android.pass.data.api.usecases.items.UpdateItemFlag
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.features.itemdetail.DetailSnackbarMessages
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.FieldCopiedToClipboard
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.InitError
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.ItemMovedToTrash
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.ItemNotMovedToTrash
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.ItemNotPermanentlyDeleted
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.ItemNotRestored
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.ItemPermanentlyDeleted
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.ItemRestored
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.PasswordCopiedToClipboard
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.TotpCopiedToClipboard
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.UsernameCopiedToClipboard
import proton.android.pass.features.itemdetail.DetailSnackbarMessages.WebsiteCopiedToClipboard
import proton.android.pass.features.itemdetail.ItemDelete
import proton.android.pass.features.itemdetail.ItemDetailNavScope
import proton.android.pass.features.itemdetail.ItemDetailScopeNavArgId
import proton.android.pass.features.itemdetail.PassMonitorItemDetailFromMissing2FA
import proton.android.pass.features.itemdetail.PassMonitorItemDetailFromReusedPassword
import proton.android.pass.features.itemdetail.PassMonitorItemDetailFromWeakPassword
import proton.android.pass.features.itemdetail.R
import proton.android.pass.features.itemdetail.common.ItemDetailEvent
import proton.android.pass.features.itemdetail.common.LoginItemFeatures
import proton.android.pass.features.itemdetail.common.ShareClickAction
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.notifications.api.SnackbarType
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.securitycenter.api.passwords.DuplicatedPasswordChecker
import proton.android.pass.securitycenter.api.passwords.InsecurePasswordChecker
import proton.android.pass.securitycenter.api.passwords.MissingTfaChecker
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.ObserveTotpFromUri
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

@Suppress("LargeClass", "LongParameterList", "TooManyFunctions")
@HiltViewModel
class LoginDetailViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val observeTotpFromUri: ObserveTotpFromUri,
    private val trashItem: TrashItems,
    private val deleteItem: DeleteItems,
    private val restoreItem: RestoreItems,
    private val getItemByAliasEmail: GetItemByAliasEmail,
    private val telemetryManager: TelemetryManager,
    private val canDisplayTotp: CanDisplayTotp,
    private val canShareShare: CanShareShare,
    private val passwordScorer: PasswordScorer,
    private val bulkMoveToVaultRepository: BulkMoveToVaultRepository,
    private val pinItem: PinItem,
    private val unpinItem: UnpinItem,
    private val updateItemFlag: UpdateItemFlag,
    private val downloadAttachment: DownloadAttachment,
    private val fileHandler: FileHandler,
    private val authOverrideState: AuthOverrideState,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    observeItemAttachments: ObserveDetailItemAttachments,
    canPerformPaidAction: CanPerformPaidAction,
    observeItemByIdWithVault: ObserveItemByIdWithVault,
    savedStateHandle: SavedStateHandleProvider,
    getItemActions: GetItemActions,
    getUserPlan: GetUserPlan,
    insecurePasswordChecker: InsecurePasswordChecker,
    duplicatedPasswordChecker: DuplicatedPasswordChecker,
    missingTfaChecker: MissingTfaChecker,
    observeShare: ObserveShare
) : ViewModel() {

    private val shareId: ShareId = savedStateHandle.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val itemId: ItemId = savedStateHandle.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private val navigationScope: ItemDetailNavScope =
        savedStateHandle.get()[ItemDetailScopeNavArgId.key]
            ?: ItemDetailNavScope.Default

    init {
        when (navigationScope) {
            ItemDetailNavScope.MonitorWeakPassword ->
                telemetryManager.sendEvent(PassMonitorItemDetailFromWeakPassword)

            ItemDetailNavScope.MonitorReusedPassword ->
                telemetryManager.sendEvent(PassMonitorItemDetailFromReusedPassword)

            ItemDetailNavScope.MonitorMissing2fa ->
                telemetryManager.sendEvent(PassMonitorItemDetailFromMissing2FA)

            ItemDetailNavScope.Default,
            ItemDetailNavScope.MonitorExcluded,
            ItemDetailNavScope.MonitorReport -> {
            }
        }
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.w(TAG, throwable)
    }

    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val isItemSentToTrashState: MutableStateFlow<IsSentToTrashState> =
        MutableStateFlow(IsSentToTrashState.NotSent)
    private val isPermanentlyDeletedState: MutableStateFlow<IsPermanentlyDeletedState> =
        MutableStateFlow(IsPermanentlyDeletedState.NotDeleted)
    private val isRestoredFromTrashState: MutableStateFlow<IsRestoredFromTrashState> =
        MutableStateFlow(IsRestoredFromTrashState.NotRestored)
    private val revealedFieldsState: MutableStateFlow<List<DetailFields>> =
        MutableStateFlow(emptyList())
    private val canPerformPaidActionFlow = canPerformPaidAction().asLoadingResult()
    private val customFieldsState: MutableStateFlow<List<CustomFieldUiContent>> =
        MutableStateFlow(emptyList())
    private val loadingAttachmentsState: MutableStateFlow<Set<AttachmentId>> =
        MutableStateFlow(emptySet())
    private val eventState: MutableStateFlow<ItemDetailEvent> =
        MutableStateFlow(ItemDetailEvent.Unknown)

    private val isSharedWithMeItemFlow = MutableStateFlow(false)

    sealed interface DetailFields {
        data object Password : DetailFields
    }

    private var hasItemBeenFetchedAtLeastOnce = false
    private val loginItemDetailsResultFlow = combine(
        observeItemByIdWithVault(shareId, itemId),
        observeShare(shareId),
        ::Pair
    ).flatMapLatest { (itemByIdWithVault, share) ->
        if (itemByIdWithVault.item.hasAttachments) {
            observeItemAttachments(shareId, itemId)
                .catch { error ->
                    PassLogger.w(TAG, "Error fetching attachments")
                    PassLogger.w(TAG, error)
                    throw error
                }
        } else {
            flowOf(emptyList())
        }.map { attachments ->
            Triple(itemByIdWithVault, attachments, share)
        }
    }
        .catch { if (!(hasItemBeenFetchedAtLeastOnce && it is ItemNotFoundError)) throw it }
        .onEach { hasItemBeenFetchedAtLeastOnce = true }
        .asLoadingResult()

    private val itemFeaturesFlow: Flow<LoginItemFeatures> = combine(
        getUserPlan(),
        featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1),
        featureFlagsRepository.get<Boolean>(FeatureFlag.ITEM_SHARING_V1)
    ) { userPlan, isFileAttachmentsEnabled, isItemSharingEnabled ->
        LoginItemFeatures(
            isHistoryEnabled = userPlan.isPaidPlan,
            isFileAttachmentsEnabled = isFileAttachmentsEnabled,
            isItemSharingEnabled = isItemSharingEnabled
        )
    }

    private val loginItemInfoFlow: Flow<LoadingResult<LoginItemInfo>> = combine(
        loginItemDetailsResultFlow,
        canPerformPaidActionFlow
    ) { detailsResult, paidActionResult ->
        paidActionResult.flatMap { isPaid ->
            detailsResult.map { (details, attachments, share) ->
                val itemType = details.item.itemType as ItemType.Login
                val alias = getAliasForItem(itemType)

                val (itemUiModel, passwordScore) = encryptionContextProvider.withEncryptionContext {
                    val model = details.item.toUiModel(this@withEncryptionContext)
                    val contents = model.contents as ItemContents.Login

                    val isPasswordEmpty =
                        decrypt(contents.password.encrypted.toEncryptedByteArray())
                            .isEmpty()
                    val passwordHiddenState = if (isPasswordEmpty) {
                        HiddenState.Empty(encrypt(""))
                    } else {
                        contents.password
                    }
                    val customFields = contents.customFields
                        .map { customField ->
                            if (customField is CustomFieldContent.Hidden) {
                                val isCustomFieldEmpty =
                                    decrypt(customField.value.encrypted.toEncryptedByteArray())
                                        .isEmpty()
                                if (isCustomFieldEmpty) {
                                    customField.copy(value = HiddenState.Empty(encrypt("")))
                                } else {
                                    customField
                                }
                            } else {
                                customField
                            }
                        }

                    val decryptedPassword = decrypt(contents.password.encrypted)
                    val passwordScore = if (decryptedPassword.isBlank()) {
                        null
                    } else {
                        passwordScorer.check(decryptedPassword)
                    }

                    model.copy(
                        contents = contents.copy(
                            password = passwordHiddenState,
                            customFields = customFields
                        )
                    ) to passwordScore
                }
                startObservingTotpCustomFields(isPaid, itemUiModel)

                val canShareVault = details.vault
                    ?.let { vault -> canShareShare(vault.shareId).value }
                    ?: false

                val shareClickAction = when {
                    isPaid && canShareVault -> ShareClickAction.Share
                    else -> ShareClickAction.Upgrade
                }

                LoginItemInfo(
                    itemUiModel = itemUiModel,
                    itemContents = itemUiModel.contents as ItemContents.Login,
                    share = share,
                    hasMoreThanOneVault = details.hasMoreThanOneVault,
                    canPerformItemActions = details.canPerformItemActions,
                    linkedAlias = alias,
                    shareClickAction = shareClickAction,
                    passwordScore = passwordScore,
                    attachments = attachments,
                    securityState = LoginMonitorState(
                        isExcludedFromMonitor = details.item.hasSkippedHealthCheck,
                        navigationScope = navigationScope,
                        insecurePasswordsReport = insecurePasswordChecker(listOf(details.item)),
                        duplicatedPasswordsReport = duplicatedPasswordChecker(details.item),
                        missing2faReport = missingTfaChecker(listOf(details.item)),
                        encryptionContextProvider = encryptionContextProvider
                    )
                )
            }
        }

    }.distinctUntilChanged()

    private val revealedLoginItemInfoFlow: Flow<LoadingResult<LoginItemInfo>> = combine(
        loginItemInfoFlow,
        revealedFieldsState
    ) { loginItemResult, revealed ->
        loginItemResult.map { item ->
            encryptionContextProvider.withEncryptionContext {
                val contents =
                    (item.itemUiModel.contents as ItemContents.Login).let { loginContents ->
                        val updatedPassword = if (revealed.contains(DetailFields.Password)) {
                            HiddenState.Revealed(
                                loginContents.password.encrypted,
                                decrypt(loginContents.password.encrypted)
                            )
                        } else {
                            loginContents.password
                        }

                        val updatedPrimaryTotp = HiddenState.Revealed(
                            loginContents.primaryTotp.encrypted,
                            decrypt(loginContents.primaryTotp.encrypted)
                        )

                        loginContents.copy(
                            password = updatedPassword,
                            primaryTotp = updatedPrimaryTotp
                        )
                    }

                item.copy(itemUiModel = item.itemUiModel.copy(contents = contents))
            }
        }
    }.distinctUntilChanged()

    private val totpUiStateFlow: Flow<TotpUiState> =
        revealedLoginItemInfoFlow
            .flatMapLatest { result ->
                val loginItemInfo = when (result) {
                    is LoadingResult.Error -> return@flatMapLatest flowOf(TotpUiState.Hidden)
                    LoadingResult.Loading -> return@flatMapLatest flowOf(TotpUiState.Hidden)
                    is LoadingResult.Success -> result.data
                }
                val contents = loginItemInfo.itemUiModel.contents as ItemContents.Login
                val decryptedTotpUri = when (val primaryTotp = contents.primaryTotp) {
                    is HiddenState.Concealed -> null
                    is HiddenState.Revealed -> primaryTotp.clearText
                    is HiddenState.Empty -> null
                }
                if (!decryptedTotpUri.isNullOrBlank()) {
                    observeTotp(decryptedTotpUri)
                } else {
                    flowOf(TotpUiState.Hidden)
                }
            }
            .distinctUntilChanged()

    private data class LoginItemInfo(
        val itemUiModel: ItemUiModel,
        val itemContents: ItemContents.Login,
        val share: Share,
        val hasMoreThanOneVault: Boolean,
        val canPerformItemActions: Boolean,
        val linkedAlias: Option<LinkedAliasItem>,
        val shareClickAction: ShareClickAction,
        val passwordScore: PasswordScore?,
        val securityState: LoginMonitorState,
        val attachments: List<Attachment>
    )

    private val loadingStates = combine(
        isLoadingState,
        loadingAttachmentsState,
        ::Pair
    )

    internal val uiState: StateFlow<LoginDetailUiState> = combineN(
        revealedLoginItemInfoFlow,
        totpUiStateFlow,
        loadingStates,
        isItemSentToTrashState,
        isPermanentlyDeletedState,
        isRestoredFromTrashState,
        canPerformPaidActionFlow,
        customFieldsState,
        oneShot { getItemActions(shareId = shareId, itemId = itemId) }.asLoadingResult(),
        eventState,
        itemFeaturesFlow
    ) { itemDetails,
        totpUiState,
        (isLoading, loadingAttachments),
        isItemSentToTrash,
        isPermanentlyDeleted,
        isRestoredFromTrash,
        canPerformPaidActionResult,
        customFields,
        itemActions,
        event,
        itemFeatures ->
        when (itemDetails) {
            is LoadingResult.Error -> {
                when {
                    isSharedWithMeItemFlow.value -> {
                        LoginDetailUiState.NotInitialised
                    }

                    !isPermanentlyDeleted.value() -> {
                        snackbarDispatcher(InitError)
                        LoginDetailUiState.Error
                    }

                    else -> {
                        LoginDetailUiState.Pending
                    }
                }
            }

            LoadingResult.Loading -> LoginDetailUiState.NotInitialised
            is LoadingResult.Success -> {
                val details = itemDetails.data
                val actions = itemActions.getOrNull() ?: ItemActions.Disabled
                val isPaid = canPerformPaidActionResult.getOrNull() == true

                val customFieldsList = if (!isPaid) emptyList() else customFields

                val passkeys = details.itemContents.passkeys.map { UIPasskeyContent.from(it) }

                isSharedWithMeItemFlow.update { details.itemUiModel.isSharedWithMe }

                LoginDetailUiState.Success(
                    itemUiModel = details.itemUiModel,
                    passwordScore = details.passwordScore,
                    share = details.share,
                    linkedAlias = details.linkedAlias,
                    totpUiState = totpUiState,
                    isLoading = isLoading.value(),
                    isItemSentToTrash = isItemSentToTrash.value(),
                    isPermanentlyDeleted = isPermanentlyDeleted.value(),
                    isRestoredFromTrash = isRestoredFromTrash.value(),
                    canPerformItemActions = details.canPerformItemActions,
                    customFields = customFieldsList.toPersistentList(),
                    passkeys = passkeys.toPersistentList(),
                    shareClickAction = details.shareClickAction,
                    itemActions = actions,
                    event = event,
                    itemFeatures = itemFeatures,
                    monitorState = details.securityState,
                    attachmentsState = AttachmentsState(
                        draftAttachmentsList = emptyList(), // no drafts in detail
                        attachmentsList = details.attachments,
                        loadingAttachments = loadingAttachments,
                        needsUpgrade = None
                    ),
                    hasMoreThanOneVault = details.hasMoreThanOneVault
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LoginDetailUiState.NotInitialised
        )

    fun copyPasswordToClipboard() = viewModelScope.launch(coroutineExceptionHandler) {
        val state = uiState.value as? LoginDetailUiState.Success ?: return@launch
        val contents = state.itemUiModel.contents as? ItemContents.Login ?: return@launch
        val text = when (val password = contents.password) {
            is HiddenState.Revealed -> password.clearText
            is HiddenState.Concealed -> encryptionContextProvider.withEncryptionContext {
                decrypt(contents.password.encrypted)
            }

            is HiddenState.Empty -> ""
        }
        clipboardManager.copyToClipboard(text = text, isSecure = true)
        snackbarDispatcher(PasswordCopiedToClipboard)
    }

    internal fun copyEmailToClipboard(email: String) = viewModelScope.launch {
        clipboardManager.copyToClipboard(email)
        snackbarDispatcher(DetailSnackbarMessages.EmailCopiedToClipboard)
    }

    fun copyUsernameToClipboard() = viewModelScope.launch {
        val state = uiState.value as? LoginDetailUiState.Success ?: return@launch
        val itemType = state.itemUiModel.contents as? ItemContents.Login ?: return@launch
        clipboardManager.copyToClipboard(itemType.itemUsername)
        snackbarDispatcher(UsernameCopiedToClipboard)
    }

    fun copyWebsiteToClipboard(website: String) = viewModelScope.launch {
        clipboardManager.copyToClipboard(website)
        snackbarDispatcher(WebsiteCopiedToClipboard)
    }

    fun copyTotpCodeToClipboard(code: String) = viewModelScope.launch {
        clipboardManager.copyToClipboard(code)
        snackbarDispatcher(TotpCopiedToClipboard)
    }

    fun togglePassword() = viewModelScope.launch(coroutineExceptionHandler) {
        revealedFieldsState.update {
            if (it.contains(DetailFields.Password)) {
                it.toMutableList().apply { remove(DetailFields.Password) }
            } else {
                it.toMutableList().apply { add(DetailFields.Password) }
            }
        }
    }

    fun onMoveToTrash(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        runCatching { trashItem(items = mapOf(shareId to listOf(itemId))) }
            .onSuccess {
                isItemSentToTrashState.update { IsSentToTrashState.Sent }
                snackbarDispatcher(ItemMovedToTrash)
            }
            .onFailure {
                snackbarDispatcher(ItemNotMovedToTrash)
                PassLogger.d(TAG, it, "Could not delete item")
            }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    internal fun pinItem(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }

        runCatching { pinItem.invoke(shareId, itemId) }
            .onSuccess { snackbarDispatcher(DetailSnackbarMessages.ItemPinnedSuccess) }
            .onFailure { error ->
                PassLogger.w(TAG, error, "An error occurred pinning Login item")
                snackbarDispatcher(DetailSnackbarMessages.ItemPinnedError)
            }

        isLoadingState.update { IsLoadingState.NotLoading }
    }

    internal fun unpinItem(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }

        runCatching { unpinItem.invoke(shareId, itemId) }
            .onSuccess { snackbarDispatcher(DetailSnackbarMessages.ItemUnpinnedSuccess) }
            .onFailure { error ->
                PassLogger.w(TAG, error, "An error occurred unpinning Login item")
                snackbarDispatcher(DetailSnackbarMessages.ItemUnpinnedError)
            }

        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun onPermanentlyDelete(itemUiModel: ItemUiModel) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        runCatching {
            deleteItem(items = mapOf(itemUiModel.shareId to listOf(itemUiModel.id)))
        }.onSuccess {
            telemetryManager.sendEvent(ItemDelete(EventItemType.from(itemUiModel.contents)))
            isPermanentlyDeletedState.update { IsPermanentlyDeletedState.Deleted }
            snackbarDispatcher(ItemPermanentlyDeleted)
            PassLogger.i(TAG, "Item deleted successfully")
        }.onFailure {
            snackbarDispatcher(ItemNotPermanentlyDeleted)
            PassLogger.i(TAG, it, "Could not delete item")
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun onItemRestore(shareId: ShareId, itemId: ItemId) = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        runCatching {
            restoreItem(items = mapOf(shareId to listOf(itemId)))
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

    @Suppress("ComplexMethod")
    fun copyCustomFieldValue(index: Int) = viewModelScope.launch {
        val state = uiState.value as? LoginDetailUiState.Success ?: return@launch
        val itemContents = state.itemUiModel.contents as? ItemContents.Login ?: return@launch
        if (index >= itemContents.customFields.size) return@launch

        val (content, isSecure) = when (val field = itemContents.customFields[index]) {
            is CustomFieldContent.Hidden -> {
                when (val value = field.value) {
                    is HiddenState.Concealed -> encryptionContextProvider.withEncryptionContext {
                        decrypt(value.encrypted)
                    }

                    is HiddenState.Revealed -> value.clearText
                    is HiddenState.Empty -> ""
                } to true
            }

            is CustomFieldContent.Text -> field.value to false
            is CustomFieldContent.Totp -> {
                val totpUri = when (val value = field.value) {
                    is HiddenState.Concealed -> encryptionContextProvider.withEncryptionContext {
                        decrypt(value.encrypted)
                    }

                    is HiddenState.Revealed -> value.clearText
                    is HiddenState.Empty -> ""
                }

                val totpCode = observeTotpFromUri(totpUri).firstOrNull()?.code ?: ""
                totpCode to false
            }
            is CustomFieldContent.Date -> throw IllegalStateException("Date field not supported")
        }

        if (content.isNotEmpty()) {
            copyCustomFieldContent(content, isSecure)
        }
    }

    fun copyCustomFieldContent(content: String, isSecure: Boolean = false) = viewModelScope.launch {
        clipboardManager.copyToClipboard(content, isSecure = isSecure)
        snackbarDispatcher(FieldCopiedToClipboard)
    }

    fun toggleCustomFieldVisibility(index: Int) = viewModelScope.launch {
        customFieldsState.update { fields ->
            val asMutable = fields.toMutableList()
            val updated = when (val field = fields[index]) {
                is CustomFieldUiContent.Limited,
                is CustomFieldUiContent.Text,
                is CustomFieldUiContent.Totp -> field

                // It only applies to Hidden custom fields
                is CustomFieldUiContent.Hidden -> {
                    val content = when (val content = field.content) {
                        is HiddenState.Concealed -> {
                            encryptionContextProvider.withEncryptionContext {
                                HiddenState.Revealed(
                                    encrypted = content.encrypted,
                                    clearText = decrypt(content.encrypted)
                                )
                            }
                        }

                        is HiddenState.Revealed -> HiddenState.Concealed(encrypted = content.encrypted)
                        is HiddenState.Empty -> HiddenState.Empty(encrypted = content.encrypted)
                    }

                    CustomFieldUiContent.Hidden(
                        label = field.label,
                        content = content
                    )
                }
            }
            asMutable[index] = updated
            asMutable.toPersistentList()
        }
    }

    internal fun clearEvent() {
        eventState.update { ItemDetailEvent.Unknown }
    }

    internal fun onMigrate() {
        val state = uiState.value as? LoginDetailUiState.Success ?: return

        viewModelScope.launch {
            bulkMoveToVaultRepository.save(mapOf(shareId to listOf(itemId)))

            if (state.itemUiModel.isShared) {
                ItemDetailEvent.MoveToVaultSharedWarning
            } else {
                ItemDetailEvent.MoveToVault
            }.also { event ->
                eventState.update { event }
            }
        }
    }

    internal fun onExcludeItemFromMonitoring() = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }

        runCatching {
            updateItemFlag(
                shareId = shareId,
                itemId = itemId,
                flag = ItemFlag.SkipHealthCheck,
                isFlagEnabled = true
            )
        }.onFailure { error ->
            PassLogger.w(TAG, "Error excluding item from monitoring")
            PassLogger.w(TAG, error)
            snackbarDispatcher(DetailSnackbarMessages.ItemMonitorExcludedError)
        }.onSuccess {
            snackbarDispatcher(DetailSnackbarMessages.ItemMonitorExcludedSuccess)
        }

        isLoadingState.update { IsLoadingState.NotLoading }
    }

    internal fun onIncludeItemInMonitoring() = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }

        runCatching {
            updateItemFlag(
                shareId = shareId,
                itemId = itemId,
                flag = ItemFlag.SkipHealthCheck,
                isFlagEnabled = false
            )
        }.onFailure { error ->
            PassLogger.w(TAG, "Error including item in monitoring")
            PassLogger.w(TAG, error)
            snackbarDispatcher(DetailSnackbarMessages.ItemMonitorIncludedError)
        }.onSuccess {
            snackbarDispatcher(DetailSnackbarMessages.ItemMonitorIncludedSuccess)
        }

        isLoadingState.update { IsLoadingState.NotLoading }
    }

    private fun observeTotp(decryptedTotpUri: String): Flow<TotpUiState> =
        canDisplayTotp(shareId = shareId, itemId = itemId)
            .flatMapLatest { canDisplay ->
                if (canDisplay) {
                    observeTotpValue(decryptedTotpUri)
                } else {
                    flowOf(TotpUiState.Limited)
                }
            }

    private fun observeTotpValue(decryptedTotpUri: String): Flow<TotpUiState> = observeTotpFromUri(decryptedTotpUri)
        .map(TotpManager.TotpWrapper::toOption)
        .map { totpValue ->
            when (totpValue) {
                None -> TotpUiState.Hidden
                is Some -> TotpUiState.Visible(
                    code = totpValue.value.code,
                    remainingSeconds = totpValue.value.remainingSeconds,
                    totalSeconds = totpValue.value.totalSeconds
                )
            }
        }
        .catch { e ->
            PassLogger.w(TAG, "Error observing totp")
            PassLogger.w(TAG, e)
            snackbarDispatcher(DetailSnackbarMessages.GenerateTotpError)
            emit(TotpUiState.Hidden)
        }

    private suspend fun getAliasForItem(item: ItemType.Login): Option<LinkedAliasItem> {
        val username = item.itemEmail
        if (username.isBlank()) return None

        return runCatching { getItemByAliasEmail(aliasEmail = username) }
            .fold(
                onSuccess = {
                    if (it == null) {
                        None
                    } else {
                        Some(LinkedAliasItem(shareId = it.shareId, itemId = it.id))
                    }
                },
                onFailure = {
                    PassLogger.w(TAG, "Error fetching alias for item")
                    PassLogger.w(TAG, it)
                    None
                }
            )
    }

    private fun startObservingTotpCustomFields(canSeeCustomFields: Boolean, itemUiModel: ItemUiModel) {
        viewModelScope.launch {
            val asLogin = itemUiModel.contents as? ItemContents.Login
            if (asLogin != null) {
                observeTotpCustomFields(canSeeCustomFields, asLogin)
            }
        }
    }

    private fun observeTotpCustomFields(canSeeCustomFields: Boolean, content: ItemContents.Login) {
        val contents = content.customFields.mapIndexed { idx, field ->
            when (field) {
                is CustomFieldContent.Hidden -> if (canSeeCustomFields) {
                    CustomFieldUiContent.Hidden(
                        label = field.label,
                        content = field.value
                    )
                } else {
                    CustomFieldUiContent.Limited.Hidden(field.label)
                }

                is CustomFieldContent.Text -> if (canSeeCustomFields) {
                    CustomFieldUiContent.Text(
                        label = field.label,
                        content = field.value
                    )
                } else {
                    CustomFieldUiContent.Limited.Text(field.label)
                }

                is CustomFieldContent.Totp -> if (canSeeCustomFields) {
                    observeTotpCustomField(idx, field)

                    CustomFieldUiContent.Totp(
                        label = field.label,
                        code = "",
                        remainingSeconds = 0,
                        totalSeconds = 10
                    )
                } else {
                    CustomFieldUiContent.Limited.Totp(label = field.label)
                }

                is CustomFieldContent.Date ->
                    throw IllegalStateException("Date field not supported")
            }
        }

        customFieldsState.update { contents }
    }

    private fun observeTotpCustomField(index: Int, field: CustomFieldContent.Totp) {
        viewModelScope.launch {
            val decryptedUri = encryptionContextProvider.withEncryptionContext {
                decrypt(field.value.encrypted)
            }

            observeTotpValue(decryptedUri).collect { totpState ->
                if (totpState is TotpUiState.Visible) {
                    customFieldsState.update { fieldsList ->
                        val mutableList = fieldsList.toMutableList()

                        mutableList[index] = CustomFieldUiContent.Totp(
                            label = field.label,
                            code = totpState.code,
                            remainingSeconds = totpState.remainingSeconds,
                            totalSeconds = totpState.totalSeconds
                        )

                        mutableList
                    }
                }
            }
        }
    }

    fun onAttachmentOpen(contextHolder: ClassHolder<Context>, attachment: Attachment) {
        viewModelScope.launch {
            loadingAttachmentsState.update { it + attachment.id }
            authOverrideState.setAuthOverride(true)
            runCatching {
                val uri = downloadAttachment(attachment)
                fileHandler.openFile(
                    contextHolder = contextHolder,
                    uri = uri,
                    mimeType = attachment.mimeType,
                    chooserTitle = contextHolder.get().value()?.getString(R.string.open_with) ?: ""
                )
            }.onSuccess {
                PassLogger.i(TAG, "Attachment opened: ${attachment.id}")
            }.onFailure {
                PassLogger.w(TAG, "Could not open attachment: ${attachment.id}")
                PassLogger.w(TAG, it)
                val message = if (it is ApiException && !it.message.isNullOrBlank()) {
                    SnackbarMessage.SimpleMessage(it.message.orEmpty(), SnackbarType.ERROR)
                } else {
                    DetailSnackbarMessages.OpenAttachmentsError
                }
                snackbarDispatcher(message)
            }
            loadingAttachmentsState.update { it - attachment.id }
        }
    }

    companion object {
        private const val TAG = "LoginDetailViewModel"
    }
}
