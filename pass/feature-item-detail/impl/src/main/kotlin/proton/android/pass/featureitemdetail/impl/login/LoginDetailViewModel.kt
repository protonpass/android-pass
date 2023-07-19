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

package proton.android.pass.featureitemdetail.impl.login

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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.clipboard.api.ClipboardManager
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
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsPermanentlyDeletedState
import proton.android.pass.composecomponents.impl.uievents.IsRestoredFromTrashState
import proton.android.pass.composecomponents.impl.uievents.IsSentToTrashState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.data.api.usecases.CanDisplayTotp
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.DeleteItem
import proton.android.pass.data.api.usecases.GetItemByAliasEmail
import proton.android.pass.data.api.usecases.GetItemByIdWithVault
import proton.android.pass.data.api.usecases.RestoreItem
import proton.android.pass.data.api.usecases.TrashItem
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.FieldCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.InitError
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemMovedToTrash
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemNotMovedToTrash
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemNotPermanentlyDeleted
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemNotRestored
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemPermanentlyDeleted
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.ItemRestored
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.PasswordCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.TotpCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.UsernameCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.WebsiteCopiedToClipboard
import proton.android.pass.featureitemdetail.impl.ItemDelete
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.ObserveTotpFromUri
import proton.android.pass.totp.api.TotpManager
import proton.pass.domain.CustomFieldContent
import proton.pass.domain.HiddenState
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import javax.inject.Inject

@Suppress("LargeClass")
@HiltViewModel
class LoginDetailViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val observeTotpFromUri: ObserveTotpFromUri,
    private val trashItem: TrashItem,
    private val deleteItem: DeleteItem,
    private val restoreItem: RestoreItem,
    private val getItemByAliasEmail: GetItemByAliasEmail,
    private val telemetryManager: TelemetryManager,
    private val canDisplayTotp: CanDisplayTotp,
    canPerformPaidAction: CanPerformPaidAction,
    getItemByIdWithVault: GetItemByIdWithVault,
    savedStateHandle: SavedStateHandleProvider
) : ViewModel() {

    private val shareId: ShareId =
        ShareId(savedStateHandle.get().require(CommonNavArgId.ShareId.key))
    private val itemId: ItemId =
        ItemId(savedStateHandle.get().require(CommonNavArgId.ItemId.key))

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
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

    sealed interface DetailFields {
        object Password : DetailFields
    }

    private val loginItemInfoFlow: Flow<LoadingResult<LoginItemInfo>> = combine(
        // REFACTOR: This take(1) is added in order to avoid flow re-emitting of the item in case
        // we receive a BE event that updates the item. That would cause the jobs that observe the
        // TOTP custom fields to be rescheduled, but the previous ones would not be cancelled
        // We should try to keep a reference to the running jobs and cancel them if this flow
        // re-emits
        getItemByIdWithVault(shareId, itemId).take(1).asLoadingResult(),
        canPerformPaidActionFlow
    ) { detailsResult, paidActionResult ->
        paidActionResult.flatMap { canShowCustomFields ->
            detailsResult.map { details ->
                val itemType = details.item.itemType as ItemType.Login
                val alias = getAliasForItem(itemType)

                val itemUiModel = encryptionContextProvider.withEncryptionContext {
                    val model = details.item.toUiModel(this)
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

                    model.copy(
                        contents = contents.copy(
                            password = passwordHiddenState,
                            customFields = customFields
                        )
                    )
                }
                startObservingTotpCustomFields(canShowCustomFields, itemUiModel)

                LoginItemInfo(
                    itemUiModel = itemUiModel,
                    vault = details.vault,
                    hasMoreThanOneVault = details.hasMoreThanOneVault,
                    linkedAlias = alias
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
        val vault: Vault,
        val hasMoreThanOneVault: Boolean,
        val linkedAlias: Option<LinkedAliasItem>
    )

    val uiState: StateFlow<LoginDetailUiState> = combineN(
        revealedLoginItemInfoFlow,
        totpUiStateFlow,
        isLoadingState,
        isItemSentToTrashState,
        isPermanentlyDeletedState,
        isRestoredFromTrashState,
        canPerformPaidActionFlow,
        customFieldsState
    ) { itemDetails,
        totpUiState,
        isLoading,
        isItemSentToTrash,
        isPermanentlyDeleted,
        isRestoredFromTrash,
        canPerformPaidActionResult,
        customFields ->
        when (itemDetails) {
            is LoadingResult.Error -> {
                snackbarDispatcher(InitError)
                LoginDetailUiState.Error
            }

            LoadingResult.Loading -> LoginDetailUiState.NotInitialised
            is LoadingResult.Success -> {
                val details = itemDetails.data
                val vault = if (details.hasMoreThanOneVault) {
                    details.vault
                } else {
                    null
                }

                val isPaid = canPerformPaidActionResult.getOrNull() == true
                val canMigrate = when {
                    isPaid -> true
                    vault?.isPrimary == true -> false
                    else -> true
                }

                val customFieldsList = if (!isPaid) emptyList() else customFields

                LoginDetailUiState.Success(
                    itemUiModel = details.itemUiModel,
                    vault = vault,
                    linkedAlias = details.linkedAlias,
                    totpUiState = totpUiState,
                    isLoading = isLoading.value(),
                    isItemSentToTrash = isItemSentToTrash.value(),
                    isPermanentlyDeleted = isPermanentlyDeleted.value(),
                    isRestoredFromTrash = isRestoredFromTrash.value(),
                    canMigrate = canMigrate,
                    customFields = customFieldsList.toPersistentList()
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

    fun copyUsernameToClipboard() = viewModelScope.launch {
        val state = uiState.value as? LoginDetailUiState.Success ?: return@launch
        val itemType = state.itemUiModel.contents as? ItemContents.Login ?: return@launch
        clipboardManager.copyToClipboard(itemType.username)
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
        runCatching { trashItem(shareId = shareId, itemId = itemId) }
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

    fun onPermanentlyDelete(itemUiModel: ItemUiModel) =
        viewModelScope.launch {
            isLoadingState.update { IsLoadingState.Loading }
            runCatching {
                deleteItem(shareId = itemUiModel.shareId, itemId = itemUiModel.id)
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

    private fun observeTotp(decryptedTotpUri: String): Flow<TotpUiState> =
        canDisplayTotp(shareId = shareId, itemId = itemId)
            .flatMapLatest { canDisplay ->
                if (canDisplay) {
                    observeTotpValue(decryptedTotpUri)
                } else {
                    flowOf(TotpUiState.Limited)
                }
            }

    private fun observeTotpValue(
        decryptedTotpUri: String
    ): Flow<TotpUiState> = observeTotpFromUri(decryptedTotpUri)
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
            PassLogger.w(TAG, e, "Error observing totp")
            snackbarDispatcher(DetailSnackbarMessages.GenerateTotpError)
            emit(TotpUiState.Hidden)
        }

    private suspend fun getAliasForItem(item: ItemType.Login): Option<LinkedAliasItem> {
        val username = item.username
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
                    PassLogger.w(TAG, it, "Error fetching alias for item")
                    None
                }
            )
    }

    private fun startObservingTotpCustomFields(
        canSeeCustomFields: Boolean,
        itemUiModel: ItemUiModel
    ) {
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

    companion object {
        private const val TAG = "LoginDetailViewModel"
    }
}
