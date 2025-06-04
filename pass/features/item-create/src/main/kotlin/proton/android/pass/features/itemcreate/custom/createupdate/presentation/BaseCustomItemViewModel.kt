/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.custom.createupdate.presentation

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.attachments.LinkAttachmentsToItem
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.WifiSecurityType
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.DraftFormFieldEvent
import proton.android.pass.features.itemcreate.common.DraftFormSectionEvent
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIExtraSection
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.ValidationError
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandler
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldIdentifier
import proton.android.pass.features.itemcreate.common.formprocessor.CustomItemFormProcessor
import proton.android.pass.features.itemcreate.common.formprocessor.FormProcessingResult
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.OnCustomFieldChanged
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.OnTitleChanged
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.DisplayFileAttachmentsBanner.NotDisplay
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.totp.api.TotpManager
import java.net.URI

sealed interface BaseItemFormIntent

sealed interface BaseCustomItemCommonIntent : BaseItemFormIntent {

    @JvmInline
    value class OnTitleChanged(val value: String) : BaseCustomItemCommonIntent

    @JvmInline
    value class OnSSIDChanged(val value: String) : BaseCustomItemCommonIntent

    @JvmInline
    value class OnWifiSecurityTypeChanged(val value: Int) : BaseCustomItemCommonIntent

    @JvmInline
    value class OnPasswordChanged(val value: String) : BaseCustomItemCommonIntent

    @JvmInline
    value class OnPublicKeyChanged(val value: String) : BaseCustomItemCommonIntent

    @JvmInline
    value class OnPrivateKeyChanged(val value: String) : BaseCustomItemCommonIntent

    @JvmInline
    value class OnPasswordFocusedChanged(val isFocused: Boolean) : BaseCustomItemCommonIntent

    @JvmInline
    value class OnPrivateKeyFocusedChanged(val isFocused: Boolean) : BaseCustomItemCommonIntent

    data class OnCustomFieldChanged(
        val field: CustomFieldIdentifier,
        val value: String
    ) : BaseCustomItemCommonIntent

    data class OnCustomFieldFocusedChanged(
        val field: CustomFieldIdentifier,
        val isFocused: Boolean
    ) : BaseCustomItemCommonIntent

    data object ClearDraft : BaseCustomItemCommonIntent

    data object ViewModelObserve : BaseCustomItemCommonIntent

    data class OnOpenDraftAttachment(
        val contextHolder: ClassHolder<Context>,
        val uri: URI,
        val mimetype: String
    ) : BaseCustomItemCommonIntent

    @JvmInline
    value class OnRetryUploadAttachment(val metadata: FileMetadata) : BaseCustomItemCommonIntent

    data object DismissFileAttachmentsBanner : BaseCustomItemCommonIntent

    data object PasteTOTPSecret : BaseCustomItemCommonIntent

    data class OnReceiveTotp(
        val uri: String,
        val sectionIndex: Option<Int>,
        val index: Int
    ) : BaseCustomItemCommonIntent

    data class OnReceiveWifiSecurityType(val type: WifiSecurityType) : BaseCustomItemCommonIntent
}

@Suppress("TooManyFunctions", "LargeClass")
abstract class BaseCustomItemViewModel(
    private val linkAttachmentsToItem: LinkAttachmentsToItem,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val customFieldDraftRepository: CustomFieldDraftRepository,
    private val attachmentsHandler: AttachmentsHandler,
    private val customFieldHandler: CustomFieldHandler,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val featureFlagsRepository: FeatureFlagsPreferencesRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val clipboardManager: ClipboardManager,
    private val totpManager: TotpManager,
    private val appDispatchers: AppDispatchers,
    private val canPerformPaidAction: CanPerformPaidAction,
    private val customItemFormProcessor: CustomItemFormProcessor,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    init {
        processCommonIntent(BaseCustomItemCommonIntent.ViewModelObserve)
    }

    @OptIn(SavedStateHandleSaveableApi::class)
    internal var itemFormState: ItemFormState by savedStateHandleProvider.get()
        .saveable { mutableStateOf(ItemFormState.EMPTY) }
        protected set

    private val isLoadingState = MutableStateFlow<IsLoadingState>(IsLoadingState.NotLoading)
    private val hasUserEditedContentState = MutableStateFlow(false)
    private val validationErrorsState = MutableStateFlow(emptySet<ValidationError>())
    private val isItemSavedState = MutableStateFlow<ItemSavedState>(ItemSavedState.Unknown)
    private val focusedFieldState = MutableStateFlow<Option<CustomFieldIdentifier>>(None)

    protected fun processCommonIntent(intent: BaseCustomItemCommonIntent) {
        when (intent) {
            BaseCustomItemCommonIntent.ViewModelObserve -> onViewModelObserve()
            is OnTitleChanged -> onTitleChange(intent.value)
            is OnCustomFieldChanged -> onCustomFieldChange(intent.field, intent.value)
            BaseCustomItemCommonIntent.ClearDraft -> onClearDraft()
            is BaseCustomItemCommonIntent.OnCustomFieldFocusedChanged ->
                onCustomFieldFocusedChanged(intent.field, intent.isFocused)

            BaseCustomItemCommonIntent.DismissFileAttachmentsBanner ->
                dismissFileAttachmentsOnboardingBanner()

            is BaseCustomItemCommonIntent.OnOpenDraftAttachment ->
                openDraftAttachment(intent.contextHolder, intent.uri, intent.mimetype)

            is BaseCustomItemCommonIntent.OnRetryUploadAttachment ->
                retryUploadDraftAttachment(intent.metadata)

            is BaseCustomItemCommonIntent.OnPasswordChanged -> onPasswordChange(intent.value)
            is BaseCustomItemCommonIntent.OnPrivateKeyChanged -> onPrivateKeyChange(intent.value)
            is BaseCustomItemCommonIntent.OnPublicKeyChanged -> onPublicKeyChange(intent.value)
            is BaseCustomItemCommonIntent.OnSSIDChanged -> onSSIDChange(intent.value)
            is BaseCustomItemCommonIntent.OnWifiSecurityTypeChanged ->
                onWifiSecurityTypeChange(intent.value)
            is BaseCustomItemCommonIntent.OnPasswordFocusedChanged ->
                onPasswordFocusedChange(intent.isFocused)

            is BaseCustomItemCommonIntent.OnPrivateKeyFocusedChanged ->
                onPrivateKeyFocusedChange(intent.isFocused)

            BaseCustomItemCommonIntent.PasteTOTPSecret -> onPasteTOTPSecret()
            is BaseCustomItemCommonIntent.OnReceiveTotp ->
                onReceiveTotp(intent.uri, intent.sectionIndex, intent.index)
            is BaseCustomItemCommonIntent.OnReceiveWifiSecurityType ->
                onReceiveWifiSecurityType(intent.type)
        }
    }

    protected fun onReceiveWifiSecurityType(type: WifiSecurityType) {
        onWifiSecurityTypeChange(type.id)
    }

    private fun onReceiveTotp(
        secret: String,
        sectionIndex: Option<Int>,
        index: Int
    ) {
        onUserEditedContent()
        val field = CustomFieldIdentifier(
            sectionIndex = sectionIndex,
            index = index,
            type = CustomFieldType.Totp
        )
        updateContent(field, secret)
    }

    private fun onPasteTOTPSecret() {
        onUserEditedContent()
        viewModelScope.launch(appDispatchers.io) {
            clipboardManager.getClipboardContent()
                .onSuccess { clipboardContent ->
                    val sanitisedContent = clipboardContent
                        .replace(" ", "")
                        .replace("\n", "")

                    withContext(appDispatchers.main) {
                        val focusedField = focusedFieldState.value.value() ?: return@withContext
                        if (focusedField.type == CustomFieldType.Totp) {
                            updateContent(focusedField, sanitisedContent)
                        }
                    }
                }
                .onFailure {
                    PassLogger.w(TAG, "Failed on getting clipboard content")
                }
        }
    }

    private fun onViewModelObserve() {
        viewModelScope.launch {
            customFieldDraftRepository.observeAllEvents()
                .collectLatest {
                    onUserEditedContent()
                    when (it) {
                        is DraftFormFieldEvent.FieldAdded -> onFieldAdded(it)
                        is DraftFormFieldEvent.FieldRemoved -> onFieldRemoved(it)
                        is DraftFormFieldEvent.FieldRenamed -> onFieldRenamed(it)
                        is DraftFormSectionEvent.SectionAdded -> onSectionAdded(it)
                        is DraftFormSectionEvent.SectionRemoved -> onSectionRemoved(it)
                        is DraftFormSectionEvent.SectionRenamed -> onSectionRenamed(it)
                    }
                }
        }
        attachmentsHandler.observeNewAttachments {
            onUserEditedContent()
            viewModelScope.launch {
                isLoadingState.update { IsLoadingState.Loading }
                attachmentsHandler.uploadNewAttachment(it.metadata)
                isLoadingState.update { IsLoadingState.NotLoading }
            }
        }.launchIn(viewModelScope)
        attachmentsHandler.observeHasDeletedAttachments {
            onUserEditedContent()
        }.launchIn(viewModelScope)
        attachmentsHandler.observeHasRenamedAttachments {
            onUserEditedContent()
        }.launchIn(viewModelScope)
    }

    private fun onSectionRenamed(event: DraftFormSectionEvent.SectionRenamed) {
        val (index, newLabel) = event
        val updatedSection = itemFormState.sectionList[index].copy(title = newLabel)
        itemFormState = itemFormState.copy(
            sectionList = itemFormState.sectionList.toMutableList().apply {
                set(event.index, updatedSection)
            }
        )
    }

    private fun onSectionRemoved(event: DraftFormSectionEvent.SectionRemoved) {
        itemFormState = itemFormState.copy(
            sectionList = itemFormState.sectionList.toMutableList().apply {
                removeAt(event.index)
            }
        )
    }

    private fun onSectionAdded(event: DraftFormSectionEvent.SectionAdded) {
        itemFormState = itemFormState.copy(
            sectionList = itemFormState.sectionList + UIExtraSection(event.label, emptyList())
        )
    }

    private fun onFieldRenamed(event: DraftFormFieldEvent.FieldRenamed) {
        val (sectionIndex, index, newLabel) = event
        when (sectionIndex) {
            is Some -> {
                val section = itemFormState.sectionList[sectionIndex.value]
                val updatedCustomFields = customFieldHandler.onCustomFieldRenamed(
                    customFieldList = section.customFields,
                    index = index,
                    newLabel = newLabel
                )
                val updatedSection = section.copy(customFields = updatedCustomFields)
                itemFormState = itemFormState.copy(
                    sectionList = itemFormState.sectionList.toMutableList().apply {
                        set(sectionIndex.value, updatedSection)
                    }
                )
            }

            is None -> {
                val updated = customFieldHandler.onCustomFieldRenamed(
                    customFieldList = itemFormState.customFieldList,
                    index = index,
                    newLabel = newLabel
                )
                itemFormState = itemFormState.copy(customFieldList = updated)
            }
        }
    }

    private fun onFieldRemoved(event: DraftFormFieldEvent.FieldRemoved) {
        val (sectionIndex, index) = event
        itemFormState = when (sectionIndex) {
            None -> itemFormState.copy(
                customFieldList = itemFormState.customFieldList.toMutableList()
                    .apply { removeAt(index) }
            )

            is Some -> {
                val section = itemFormState.sectionList[sectionIndex.value]
                val updatedSection = section.copy(
                    customFields = section.customFields.toMutableList().apply {
                        removeAt(index)
                    }
                )
                itemFormState.copy(
                    sectionList = itemFormState.sectionList.toMutableList().apply {
                        set(sectionIndex.value, updatedSection)
                    }
                )
            }
        }
    }

    private fun onFieldAdded(event: DraftFormFieldEvent.FieldAdded) {
        val (sectionIndex, label, type) = event
        val added = customFieldHandler.onCustomFieldAdded(label, type)
        when (sectionIndex) {
            is Some -> {
                val section = itemFormState.sectionList[sectionIndex.value]
                val updatedSection = section.copy(customFields = section.customFields + added)
                itemFormState = itemFormState.copy(
                    sectionList = itemFormState.sectionList.toMutableList().apply {
                        set(sectionIndex.value, updatedSection)
                    }
                )
                val identifier = CustomFieldIdentifier(
                    index = updatedSection.customFields.lastIndex,
                    type = type
                )
                focusedFieldState.update { identifier.some() }
            }

            is None -> {
                itemFormState = itemFormState.copy(
                    customFieldList = itemFormState.customFieldList + added
                )
                val identifier = CustomFieldIdentifier(
                    index = itemFormState.customFieldList.lastIndex,
                    type = type
                )
                focusedFieldState.update { identifier.some() }
            }
        }
    }

    private fun onCustomFieldFocusedChanged(customFieldIdentifier: CustomFieldIdentifier, isFocused: Boolean) {
        if (customFieldIdentifier.type == CustomFieldType.Totp) return
        when (customFieldIdentifier.sectionIndex) {
            None -> {
                val customFields = customFieldHandler.onCustomFieldFocusedChanged(
                    customFieldIdentifier = customFieldIdentifier,
                    customFieldList = itemFormState.customFieldList,
                    isFocused = isFocused
                )
                itemFormState = itemFormState.copy(customFieldList = customFields)
            }

            is Some -> {
                val sectionPos = customFieldIdentifier.sectionIndex.value() ?: 0
                if (sectionPos >= itemFormState.sectionList.size) return

                itemFormState = itemFormState.copy(
                    sectionList = itemFormState.sectionList.mapIndexed sections@{ index, section ->
                        if (index != customFieldIdentifier.index) return@sections section
                        val customFields = customFieldHandler.onCustomFieldFocusedChanged(
                            customFieldIdentifier = customFieldIdentifier,
                            customFieldList = section.customFields,
                            isFocused = isFocused
                        )
                        section.copy(customFields = customFields)
                    }
                )
            }
        }

        focusedFieldState.update {
            customFieldIdentifier
                .takeIf { isFocused }
                .toOption()
        }
    }

    private fun onClearDraft() {
        attachmentsHandler.onClearAttachments()
    }

    private fun onTitleChange(field: String) {
        onUserEditedContent()
        itemFormState = itemFormState.copy(title = field)
    }

    private fun onUserEditedContent() {
        if (!hasUserEditedContentState.value) {
            hasUserEditedContentState.update { true }
        }
    }

    protected suspend fun isFormStateValid(
        originalCustomFields: List<UICustomFieldContent> = emptyList(),
        originalSections: List<UIExtraSection> = emptyList()
    ): Boolean {
        val result = encryptionContextProvider.withEncryptionContextSuspendable {
            customItemFormProcessor.process(
                CustomItemFormProcessor.Input(
                    formState = itemFormState,
                    originalCustomFields = originalCustomFields,
                    originalSections = originalSections
                ),
                ::decrypt,
                ::encrypt
            )
        }
        return when (result) {
            is FormProcessingResult.Error -> {
                validationErrorsState.update { result.errors }
                false
            }
            is FormProcessingResult.Success -> {
                itemFormState = result.sanitized
                true
            }
        }
    }

    protected fun updateLoadingState(isLoading: IsLoadingState) {
        isLoadingState.update { isLoading }
    }

    protected fun onItemSavedState(item: Item) {
        val itemSavedState = encryptionContextProvider.withEncryptionContext {
            ItemSavedState.Success(
                item.id,
                item.toUiModel(this@withEncryptionContext)
            )
        }
        isItemSavedState.update { itemSavedState }
    }

    private fun onCustomFieldChange(field: CustomFieldIdentifier, value: String) {
        onUserEditedContent()
        updateContent(field, value)
    }

    private fun updateContent(field: CustomFieldIdentifier, newValue: String) {
        when (field.sectionIndex) {
            None -> {
                val updated = customFieldHandler.onCustomFieldValueChanged(
                    customFieldIdentifier = field,
                    customFieldList = itemFormState.customFieldList,
                    value = newValue
                )
                itemFormState = itemFormState.copy(
                    customFieldList = updated
                )
            }

            is Some<Int> -> {
                val section =
                    itemFormState.sectionList[field.sectionIndex.value]
                val updated = customFieldHandler.onCustomFieldValueChanged(
                    customFieldIdentifier = field,
                    customFieldList = section.customFields,
                    value = newValue
                )
                val updatedSection = section.copy(customFields = updated)
                itemFormState = itemFormState.copy(
                    sectionList = itemFormState.sectionList
                        .toMutableList()
                        .apply {
                            set(
                                index = field.sectionIndex.value,
                                element = updatedSection
                            )
                        }
                )
            }
        }
    }

    private fun toggleHiddenState(hiddenState: UIHiddenState, isFocused: Boolean): UIHiddenState =
        encryptionContextProvider.withEncryptionContext {
            decrypt(hiddenState.encrypted.toEncryptedByteArray()).let { decryptedByteArray ->
                when {
                    decryptedByteArray.isEmpty() -> {
                        UIHiddenState.Empty(encrypted = hiddenState.encrypted)
                    }

                    isFocused -> {
                        UIHiddenState.Revealed(
                            encrypted = hiddenState.encrypted,
                            clearText = decryptedByteArray.decodeToString()
                        )
                    }

                    else -> {
                        UIHiddenState.Concealed(encrypted = hiddenState.encrypted)
                    }
                }
            }
        }

    private fun openDraftAttachment(
        contextHolder: ClassHolder<Context>,
        uri: URI,
        mimetype: String
    ) {
        attachmentsHandler.openDraftAttachment(contextHolder, uri, mimetype)
    }

    private fun retryUploadDraftAttachment(metadata: FileMetadata) {
        viewModelScope.launch {
            isLoadingState.update { IsLoadingState.Loading }
            attachmentsHandler.uploadNewAttachment(metadata)
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    private fun dismissFileAttachmentsOnboardingBanner() {
        viewModelScope.launch {
            userPreferencesRepository.setDisplayFileAttachmentsOnboarding(NotDisplay)
        }
    }

    protected suspend fun linkAttachments(
        shareId: ShareId,
        itemId: ItemId,
        revision: Long
    ) {
        runCatching {
            linkAttachmentsToItem(shareId, itemId, revision)
        }.onFailure {
            PassLogger.w(TAG, "Link attachment error")
            PassLogger.w(TAG, it)
            snackbarDispatcher(CustomItemSnackbarMessage.ItemLinkAttachmentsError)
        }
    }

    private fun onSSIDChange(value: String) {
        onUserEditedContent()

        val updatedStaticFields = (itemFormState.itemStaticFields as ItemStaticFields.WifiNetwork)
            .copy(ssid = value)
        itemFormState = itemFormState.copy(itemStaticFields = updatedStaticFields)
    }

    private fun onWifiSecurityTypeChange(value: Int) {
        onUserEditedContent()

        val updatedStaticFields = (itemFormState.itemStaticFields as ItemStaticFields.WifiNetwork)
            .copy(wifiSecurityType = WifiSecurityType.fromId(value))
        itemFormState = itemFormState.copy(itemStaticFields = updatedStaticFields)
    }

    private fun onPublicKeyChange(value: String) {
        onUserEditedContent()

        val updatedStaticFields = (itemFormState.itemStaticFields as ItemStaticFields.SSHKey)
            .copy(publicKey = value)
        itemFormState = itemFormState.copy(itemStaticFields = updatedStaticFields)
    }

    private fun onPrivateKeyChange(value: String) {
        onUserEditedContent()

        val privateKey = encryptionContextProvider.withEncryptionContext {
            if (value.isBlank()) {
                UIHiddenState.Empty(encrypt(""))
            } else {
                UIHiddenState.Revealed(
                    encrypted = encrypt(value),
                    clearText = value
                )
            }
        }
        val updatedStaticFields = (itemFormState.itemStaticFields as ItemStaticFields.SSHKey)
            .copy(privateKey = privateKey)
        itemFormState = itemFormState.copy(itemStaticFields = updatedStaticFields)
    }

    private fun onPasswordChange(value: String) {
        onUserEditedContent()

        val password = encryptionContextProvider.withEncryptionContext {
            if (value.isBlank()) {
                UIHiddenState.Empty(encrypt(""))
            } else {
                UIHiddenState.Revealed(
                    encrypted = encrypt(value),
                    clearText = value
                )
            }
        }
        val updatedStaticFields = (itemFormState.itemStaticFields as ItemStaticFields.WifiNetwork)
            .copy(password = password)
        itemFormState = itemFormState.copy(itemStaticFields = updatedStaticFields)
    }

    private fun onPrivateKeyFocusedChange(isFocused: Boolean) {
        val sshKeyFields = itemFormState.itemStaticFields as ItemStaticFields.SSHKey
        itemFormState = itemFormState.copy(
            itemStaticFields = sshKeyFields.copy(
                privateKey = toggleHiddenState(sshKeyFields.privateKey, isFocused)
            )
        )
    }

    private fun onPasswordFocusedChange(isFocused: Boolean) {
        val wifiNetworkFields = itemFormState.itemStaticFields as ItemStaticFields.WifiNetwork
        itemFormState = itemFormState.copy(
            itemStaticFields = wifiNetworkFields.copy(
                password = toggleHiddenState(wifiNetworkFields.password, isFocused)
            )
        )
    }

    protected fun observeSharedState(): Flow<ItemSharedUiState> = combineN(
        isLoadingState,
        hasUserEditedContentState,
        validationErrorsState,
        isItemSavedState,
        focusedFieldState,
        canPerformPaidAction(),
        featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1),
        userPreferencesRepository.observeDisplayFileAttachmentsOnboarding(),
        attachmentsHandler.attachmentState
    ) { isLoading, hasEdited, errors, savedState, lastAddedField, canPerformPaidAction,
        isFileAttachmentsEnabled, displayFileAttachmentsOnboarding, attachmentsState ->
        ItemSharedUiState(
            isLoadingState = isLoading,
            hasUserEditedContent = hasEdited,
            validationErrors = errors.toPersistentSet(),
            isItemSaved = savedState,
            focusedField = lastAddedField,
            canCreateItem = canPerformPaidAction,
            displayFileAttachmentsOnboarding = displayFileAttachmentsOnboarding.value(),
            isFileAttachmentsEnabled = isFileAttachmentsEnabled,
            attachmentsState = attachmentsState
        )
    }

    companion object {
        private const val TAG = "BaseCustomItemViewModel"
    }
}
