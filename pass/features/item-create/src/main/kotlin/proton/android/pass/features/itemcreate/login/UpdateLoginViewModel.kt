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

package proton.android.pass.features.itemcreate.login

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonrust.api.EmailValidator
import proton.android.pass.commonrust.api.passwords.strengths.PasswordStrengthCalculator
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.InvalidContentFormatVersionError
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.repositories.PendingAttachmentLinkRepository
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.UpdateItem
import proton.android.pass.data.api.usecases.attachments.LinkAttachmentsToItem
import proton.android.pass.data.api.usecases.attachments.RenameAttachments
import proton.android.pass.data.api.usecases.tooltips.DisableTooltip
import proton.android.pass.data.api.usecases.tooltips.ObserveTooltipEnabled
import proton.android.pass.data.api.work.WorkerItem
import proton.android.pass.data.api.work.WorkerLauncher
import proton.android.pass.domain.CustomField
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.areItemContentsEqual
import proton.android.pass.domain.entity.NewAlias
import proton.android.pass.domain.toItemContents
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.ItemUpdate
import proton.android.pass.features.itemcreate.MFAUpdated
import proton.android.pass.features.itemcreate.alias.AliasItemFormState
import proton.android.pass.features.itemcreate.alias.AliasMailboxUiModel
import proton.android.pass.features.itemcreate.alias.AliasSnackbarMessage
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandler
import proton.android.pass.features.itemcreate.common.formprocessor.LoginItemFormProcessorType
import proton.android.pass.features.itemcreate.login.LoginSnackbarMessages.AttachmentsInitError
import proton.android.pass.features.itemcreate.login.LoginSnackbarMessages.InitError
import proton.android.pass.features.itemcreate.login.LoginSnackbarMessages.ItemLinkAttachmentsError
import proton.android.pass.features.itemcreate.login.LoginSnackbarMessages.ItemRenameAttachmentsError
import proton.android.pass.features.itemcreate.login.LoginSnackbarMessages.ItemUpdateError
import proton.android.pass.features.itemcreate.login.LoginSnackbarMessages.UpdateAppToUpdateItemError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

@[HiltViewModel Suppress("LongParameterList")]
class UpdateLoginViewModel @Inject constructor(
    private val getItemById: ObserveItemById,
    private val updateItem: UpdateItem,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val telemetryManager: TelemetryManager,
    private val createAlias: CreateAlias,
    private val workerLauncher: WorkerLauncher,
    private val totpManager: TotpManager,
    private val linkAttachmentsToItem: LinkAttachmentsToItem,
    private val renameAttachments: RenameAttachments,
    private val pendingAttachmentLinkRepository: PendingAttachmentLinkRepository,
    passwordStrengthCalculator: PasswordStrengthCalculator,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    accountManager: AccountManager,
    clipboardManager: ClipboardManager,
    observeCurrentUser: ObserveCurrentUser,
    observeUpgradeInfo: ObserveUpgradeInfo,
    draftRepository: DraftRepository,
    emailValidator: EmailValidator,
    observeTooltipEnabled: ObserveTooltipEnabled,
    disableTooltip: DisableTooltip,
    attachmentsHandler: AttachmentsHandler,
    customFieldHandler: CustomFieldHandler,
    userPreferencesRepository: UserPreferencesRepository,
    customFieldDraftRepository: CustomFieldDraftRepository,
    loginItemFormProcessor: LoginItemFormProcessorType,
    savedStateHandleProvider: SavedStateHandleProvider
) : BaseLoginViewModel(
    accountManager = accountManager,
    snackbarDispatcher = snackbarDispatcher,
    clipboardManager = clipboardManager,
    totpManager = totpManager,
    observeCurrentUser = observeCurrentUser,
    observeUpgradeInfo = observeUpgradeInfo,
    draftRepository = draftRepository,
    encryptionContextProvider = encryptionContextProvider,
    passwordStrengthCalculator = passwordStrengthCalculator,
    emailValidator = emailValidator,
    observeTooltipEnabled = observeTooltipEnabled,
    disableTooltip = disableTooltip,
    attachmentsHandler = attachmentsHandler,
    customFieldHandler = customFieldHandler,
    userPreferencesRepository = userPreferencesRepository,
    featureFlagsRepository = featureFlagsRepository,
    customFieldDraftRepository = customFieldDraftRepository,
    loginItemFormProcessor = loginItemFormProcessor,
    savedStateHandleProvider = savedStateHandleProvider
) {
    private val navShareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val navItemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ItemId.key)
        .let(::ItemId)

    private val updateEventFlow: MutableStateFlow<UpdateUiEvent> =
        MutableStateFlow(UpdateUiEvent.Idle)

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.w(TAG, throwable)
    }

    private var itemOption: Option<Item> = None
    private var originalTotpCustomFields: List<UICustomFieldContent.Totp> = emptyList()

    init {
        viewModelScope.launch(coroutineExceptionHandler) {
            if (itemOption != None) return@launch

            isLoadingState.update { IsLoadingState.Loading }
            runCatching { getItemById.invoke(navShareId, navItemId).first() }
                .onFailure {
                    PassLogger.i(TAG, it, "Get by id error")
                    snackbarDispatcher(InitError)
                }
                .onSuccess { item ->
                    runCatching {
                        if (item.hasAttachments && isFileAttachmentsEnabled()) {
                            attachmentsHandler.getAttachmentsForItem(item.shareId, item.id)
                        }
                        item
                    }.onFailure {
                        PassLogger.w(TAG, it)
                        PassLogger.w(TAG, "Get attachments error")
                        snackbarDispatcher(AttachmentsInitError)
                    }
                    itemOption = item.some()
                    onItemReceived(item)
                }
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    internal val updateLoginUiState: StateFlow<UpdateLoginUiState> = combine(
        flowOf(navShareId),
        baseLoginUiState,
        updateEventFlow,
        ::UpdateLoginUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UpdateLoginUiState.Initial
    )

    internal fun setTotp(navTotpUri: String, navTotpIndex: Int) {
        onUserEditedContent()
        val currentState = loginItemFormState
        val primaryTotp = updatePrimaryTotpIfNeeded(navTotpUri, navTotpIndex, currentState)
        val customFields = updateCustomFieldsIfNeeded(navTotpUri, navTotpIndex, currentState)
        loginItemFormMutableState = currentState.copy(
            primaryTotp = primaryTotp,
            customFields = customFields
        )
    }

    internal fun updateItem(shareId: ShareId) = viewModelScope.launch(coroutineExceptionHandler) {
        val currentItem = itemOption.value() ?: return@launch
        val originalPrimaryTotp = UIHiddenState.Concealed((currentItem.itemType as ItemType.Login).primaryTotp)
            .some()
        if (!isFormStateValid(originalPrimaryTotp, originalTotpCustomFields)) return@launch
        isLoadingState.update { IsLoadingState.Loading }
        val loginItem = loginItemFormState.toItemContents(emailValidator = emailValidator)
        val userId = accountManager.getPrimaryUserId()
            .first { userId -> userId != null }
        if (userId != null) {
            val aliasItemOption = aliasLocalItemState.value
            if (aliasItemOption is Some) {
                performCreateAlias(userId, shareId, aliasItemOption.value)
                    .map { performUpdateItem(userId, shareId, currentItem, loginItem) }
            } else {
                performUpdateItem(userId, shareId, currentItem, loginItem)
            }
        } else {
            PassLogger.i(TAG, "Empty User Id")
            snackbarDispatcher(ItemUpdateError)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    internal fun onDeletePasskey(idx: Int, passkey: UIPasskeyContent) {
        updateEventFlow.update { UpdateUiEvent.ConfirmDeletePasskey(idx, passkey) }
    }

    internal fun onDeletePasskeyConfirmed(idx: Int, passkey: UIPasskeyContent) {
        val newPasskeyList = loginItemFormMutableState.passkeys.toMutableList()
        if (idx in 0 until newPasskeyList.size) {
            val removed = newPasskeyList.removeAt(idx)
            if (removed.id == passkey.id) {
                loginItemFormMutableState = loginItemFormState.copy(passkeys = newPasskeyList)
            }
        }
    }

    internal fun consumeEvent(event: UpdateUiEvent) {
        updateEventFlow.compareAndSet(event, UpdateUiEvent.Idle)
    }

    @Suppress("LongMethod")
    private fun onItemReceived(item: Item) {
        encryptionContextProvider.withEncryptionContext {
            val default = LoginItemFormState.default(this)
            if (loginItemFormState.compare(default, this)) {
                val itemContents = item.toItemContents<ItemContents.Login> { decrypt(it) }

                val decryptedTotp = handleTotp(
                    encryptionContext = this@withEncryptionContext,
                    primaryTotp = itemContents.primaryTotp.encrypted
                )

                val passwordHiddenState = when (val hiddenState = itemContents.password) {
                    is HiddenState.Empty -> UIHiddenState.Empty(hiddenState.encrypted)
                    is HiddenState.Concealed,
                    is HiddenState.Revealed -> UIHiddenState.Concealed(hiddenState.encrypted)
                }

                val customFields = itemContents.customFields.map(UICustomFieldContent.Companion::from)
                originalTotpCustomFields = customFields.filterIsInstance<UICustomFieldContent.Totp>()

                loginItemFormMutableState = loginItemFormState.copy(
                    title = itemContents.title,
                    email = itemContents.itemEmail,
                    username = itemContents.itemUsername,
                    password = passwordHiddenState,
                    passwordStrength = passwordStrengthCalculator.calculateStrength(
                        password = decrypt(itemContents.password.encrypted)
                    ),
                    urls = itemContents.urls.ifEmpty { listOf("") },
                    note = itemContents.note,
                    packageInfoSet = item.packageInfoSet.map(::PackageInfoUi).toSet(),
                    primaryTotp = UIHiddenState.Revealed(encrypt(decryptedTotp), decryptedTotp),
                    customFields = customFieldHandler.sanitiseForEditingCustomFields(customFields),
                    passkeys = itemContents.passkeys.map { UIPasskeyContent.from(it) },
                    isExpandedByContent = itemContents.itemEmail.isNotBlank() && itemContents.itemUsername.isNotBlank()
                )
            }
        }
    }

    private suspend fun performCreateAlias(
        userId: UserId,
        shareId: ShareId,
        aliasItemFormState: AliasItemFormState
    ): Result<Item> = if (aliasItemFormState.selectedSuffix != null) {
        runCatching {
            createAlias(
                userId = userId,
                shareId = shareId,
                newAlias = NewAlias(
                    contents = aliasItemFormState.toItemContents(),
                    prefix = aliasItemFormState.prefix,
                    suffix = aliasItemFormState.selectedSuffix.toDomain(),
                    aliasName = aliasItemFormState.senderName,
                    mailboxes = aliasItemFormState.selectedMailboxes
                        .map(AliasMailboxUiModel::toDomain)
                )
            )
        }.onFailure {
            PassLogger.w(TAG, "Error creating alias")
            PassLogger.w(TAG, it)
        }
    } else {
        val message = "Empty suffix on create alias"
        PassLogger.i(TAG, message)
        snackbarDispatcher(AliasSnackbarMessage.ItemCreationError)
        Result.failure(Exception(message))
    }

    @Suppress("LongMethod")
    private suspend fun performUpdateItem(
        userId: UserId,
        shareId: ShareId,
        currentItem: Item,
        contents: ItemContents.Login
    ) {
        runCatching {
            val hasContentsChanged = encryptionContextProvider.withEncryptionContextSuspendable {
                !areItemContentsEqual(
                    a = currentItem.toItemContents { decrypt(it) },
                    b = contents,
                    decrypt = { decrypt(it) }
                )
            }

            val hasPendingAttachments =
                pendingAttachmentLinkRepository.getAllToLink().isNotEmpty() ||
                    pendingAttachmentLinkRepository.getAllToUnLink().isNotEmpty()

            if (hasContentsChanged || hasPendingAttachments) {
                updateItem(
                    userId = userId,
                    shareId = shareId,
                    item = currentItem,
                    contents = contents
                )
            } else {
                currentItem
            }
        }.onSuccess { item ->
            snackbarDispatcher(LoginSnackbarMessages.LoginUpdated)
            if (isFileAttachmentsEnabled()) {
                runCatching {
                    renameAttachments(item.shareId, item.id)
                }.onFailure {
                    PassLogger.w(TAG, "Error renaming attachments")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(ItemRenameAttachmentsError)
                }
                runCatching {
                    linkAttachmentsToItem(item.shareId, item.id, item.revision)
                }.onFailure {
                    PassLogger.w(TAG, "Link attachment error")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(ItemLinkAttachmentsError)
                }
            }
            launchUpdateAssetLinksWorker(contents.urls.toSet())
            isItemSavedState.update {
                encryptionContextProvider.withEncryptionContext {
                    ItemSavedState.Success(
                        item.id,
                        item.toUiModel(this@withEncryptionContext)
                    )
                }
            }

            telemetryManager.sendEvent(ItemUpdate(EventItemType.Login))
            send2FAUpdatedTelemetryEvent(currentItem, item)
        }.onFailure {
            val message = if (it is InvalidContentFormatVersionError) {
                UpdateAppToUpdateItemError
            } else {
                ItemUpdateError
            }
            PassLogger.w(TAG, "Update item error")
            PassLogger.w(TAG, it)
            snackbarDispatcher(message)
        }
    }

    private fun send2FAUpdatedTelemetryEvent(currentItem: Item, updatedItem: Item) {
        if (currentItem.itemType is ItemType.Login && updatedItem.itemType is ItemType.Login) {
            encryptionContextProvider.withEncryptionContext {
                val currentItemType = currentItem.itemType as ItemType.Login
                val updatedItemType = updatedItem.itemType as ItemType.Login
                val decryptedCurrent = currentItemType.customFields
                    .filterIsInstance<CustomField.Totp>()
                    .map { decrypt(it.value) }
                val decryptedUpdated = updatedItemType.customFields
                    .filterIsInstance<CustomField.Totp>()
                    .map { decrypt(it.value) }
                if (decrypt(currentItemType.primaryTotp) != decrypt(updatedItemType.primaryTotp) ||
                    decryptedCurrent != decryptedUpdated
                ) {
                    telemetryManager.sendEvent(MFAUpdated)
                }
            }
        }
    }

    private fun handleTotp(encryptionContext: EncryptionContext, primaryTotp: EncryptedString): String {
        val totp = encryptionContext.decrypt(primaryTotp)
        if (totp.isBlank()) return totp

        itemHadTotpState.update { true }
        return getDisplayTotp(totp)
    }

    private fun getDisplayTotp(totp: String): String {
        if (totp.isBlank()) return totp

        return totpManager.sanitiseToEdit(totp).getOrNull() ?: totp
    }

    private suspend fun launchUpdateAssetLinksWorker(websites: Set<String>) {
        if (isDALEnabled()) {
            workerLauncher.launch(WorkerItem.SingleItemAssetLink(websites))
        }
    }

    private companion object {

        private const val TAG = "UpdateLoginViewModel"

    }

}
