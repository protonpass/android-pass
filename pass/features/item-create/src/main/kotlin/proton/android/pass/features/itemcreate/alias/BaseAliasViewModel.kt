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

package proton.android.pass.features.itemcreate.alias

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.alias.draftrepositories.MailboxDraftRepository
import proton.android.pass.features.itemcreate.alias.draftrepositories.SuffixDraftRepository
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.CustomFieldValidationError
import proton.android.pass.features.itemcreate.common.DraftFormFieldEvent
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.ValidationError
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandler
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldIdentifier
import proton.android.pass.features.itemcreate.common.formprocessor.AliasItemFormProcessor
import proton.android.pass.features.itemcreate.common.formprocessor.AliasItemFormProcessorType
import proton.android.pass.features.itemcreate.common.formprocessor.FormProcessingResult
import proton.android.pass.navigation.api.AliasOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.DisplayFileAttachmentsBanner
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryBannerPreference
import proton.android.pass.preferences.featurediscovery.FeatureDiscoveryFeature.AliasManagementOptions
import proton.android.pass.preferences.value
import java.net.URI

abstract class BaseAliasViewModel(
    private val mailboxDraftRepository: MailboxDraftRepository,
    private val suffixDraftRepository: SuffixDraftRepository,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val attachmentsHandler: AttachmentsHandler,
    private val featureFlagsRepository: FeatureFlagsPreferencesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val customFieldHandler: CustomFieldHandler,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val aliasItemFormProcessor: AliasItemFormProcessorType,
    canPerformPaidAction: CanPerformPaidAction,
    customFieldDraftRepository: CustomFieldDraftRepository,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val hasUserEditedContentFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.Loading)
    private val focusedFieldState: MutableStateFlow<Option<AliasField>> =
        MutableStateFlow<Option<AliasField>>(None)

    init {
        customFieldDraftRepository.observeCustomFieldEvents()
            .onEach {
                onUserEditedContent()
                when (it) {
                    is DraftFormFieldEvent.FieldAdded -> onFieldAdded(it)
                    is DraftFormFieldEvent.FieldRemoved -> onFieldRemoved(it)
                    is DraftFormFieldEvent.FieldRenamed -> onFieldRenamed(it)
                }
            }
            .launchIn(viewModelScope)
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

    private val title: Option<String> = savedStateHandleProvider.get()
        .get<String>(AliasOptionalNavArgId.Title.key)
        .toOption()
    protected var isDraft: Boolean = false

    @OptIn(SavedStateHandleSaveableApi::class)
    protected var aliasItemFormMutableState: AliasItemFormState by savedStateHandleProvider.get()
        .saveable { mutableStateOf(AliasItemFormState.default(title)) }
    val aliasItemFormState: AliasItemFormState get() = aliasItemFormMutableState

    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    protected val isAliasDraftSavedState: MutableStateFlow<AliasDraftSavedState> =
        MutableStateFlow(AliasDraftSavedState.Unknown)
    protected val aliasItemValidationErrorsState: MutableStateFlow<Set<ValidationError>> =
        MutableStateFlow(emptySet())
    protected val isApplyButtonEnabledState: MutableStateFlow<IsButtonEnabled> =
        MutableStateFlow(IsButtonEnabled.Disabled)
    protected val mutableCloseScreenEventFlow: MutableStateFlow<CloseScreenEvent> =
        MutableStateFlow(CloseScreenEvent.NotClose)

    private val eventWrapperState = combine(
        isItemSavedState,
        isAliasDraftSavedState,
        isApplyButtonEnabledState,
        mutableCloseScreenEventFlow,
        ::EventWrapper
    )

    private data class EventWrapper(
        val itemSavedState: ItemSavedState,
        val isAliasDraftSaved: AliasDraftSavedState,
        val isApplyButtonEnabled: IsButtonEnabled,
        val closeScreenEvent: CloseScreenEvent
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    internal val baseAliasUiState: StateFlow<BaseAliasUiState> = combineN(
        aliasItemValidationErrorsState,
        isLoadingState,
        eventWrapperState,
        hasUserEditedContentFlow,
        featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1),
        featureFlagsRepository.get<Boolean>(FeatureFlag.CUSTOM_TYPE_V1),
        userPreferencesRepository.observeDisplayFeatureDiscoverBanner(AliasManagementOptions),
        userPreferencesRepository.observeDisplayFileAttachmentsOnboarding(),
        attachmentsHandler.attachmentState,
        canPerformPaidAction(),
        focusedFieldState
    ) { aliasItemValidationErrors, isLoading, eventWrapper, hasUserEditedContent,
        isFileAttachmentEnabled, isCustomTypeEnabled, displayAdvancedOptionsBanner,
        displayFileAttachmentsOnboarding, attachmentsState, canPerformPaidAction, focusedField ->
        BaseAliasUiState(
            isDraft = isDraft,
            errorList = aliasItemValidationErrors,
            isLoadingState = isLoading,
            itemSavedState = eventWrapper.itemSavedState,
            isAliasDraftSavedState = eventWrapper.isAliasDraftSaved,
            isApplyButtonEnabled = eventWrapper.isApplyButtonEnabled,
            closeScreenEvent = eventWrapper.closeScreenEvent,
            hasUserEditedContent = hasUserEditedContent,
            hasReachedAliasLimit = false,
            canUpgrade = false,
            isFileAttachmentEnabled = isFileAttachmentEnabled,
            isCustomTypeEnabled = isCustomTypeEnabled,
            displayAdvancedOptionsBanner = displayAdvancedOptionsBanner.value,
            displayFileAttachmentsOnboarding = displayFileAttachmentsOnboarding.value(),
            attachmentsState = attachmentsState,
            canPerformPaidAction = canPerformPaidAction,
            focusedField = focusedField
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BaseAliasUiState.Initial
        )

    abstract fun onTitleChange(value: String)

    open fun onNoteChange(value: String) {
        onUserEditedContent()
        aliasItemFormMutableState = aliasItemFormMutableState.copy(note = value)
    }

    open fun onSLNoteChange(newSLNote: String) {
        onUserEditedContent()
        aliasItemFormMutableState = aliasItemFormMutableState.copy(slNote = newSLNote)
    }

    open fun onSenderNameChange(value: String) {
        onUserEditedContent()
        aliasItemFormMutableState = aliasItemFormMutableState.copy(senderName = value)
    }

    protected fun getAliasToBeCreated(alias: String, suffix: AliasSuffixUiModel?): String? {
        if (suffix != null && alias.isNotBlank()) {
            return "$alias${suffix.suffix}"
        }
        return null
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun setDraftStatus(status: Boolean) {
        isDraft = status
    }

    protected fun onUserEditedContent() {
        if (hasUserEditedContentFlow.value) return
        hasUserEditedContentFlow.update { true }
    }

    fun onEmitSnackbarMessage(snackbarMessage: AliasSnackbarMessage) = viewModelScope.launch {
        snackbarDispatcher(snackbarMessage)
    }

    internal fun clearDraftData() {
        attachmentsHandler.onClearAttachments()
        mailboxDraftRepository.clearMailboxes()
        suffixDraftRepository.clearSuffixes()
    }

    fun openDraftAttachment(
        contextHolder: ClassHolder<Context>,
        uri: URI,
        mimetype: String
    ) {
        attachmentsHandler.openDraftAttachment(contextHolder, uri, mimetype)
    }

    fun openAttachment(contextHolder: ClassHolder<Context>, attachment: Attachment) {
        viewModelScope.launch {
            attachmentsHandler.openAttachment(
                contextHolder = contextHolder,
                attachment = attachment
            )
        }
    }

    suspend fun isFileAttachmentsEnabled() = featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1)
        .firstOrNull()
        ?: false

    fun retryUploadDraftAttachment(metadata: FileMetadata) {
        viewModelScope.launch {
            isLoadingState.update { IsLoadingState.Loading }
            attachmentsHandler.uploadNewAttachment(metadata)
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    fun dismissFileAttachmentsOnboardingBanner() {
        viewModelScope.launch {
            userPreferencesRepository.setDisplayFileAttachmentsOnboarding(
                DisplayFileAttachmentsBanner.NotDisplay
            )
        }
    }

    fun dismissAdvancedOptionsBanner() {
        viewModelScope.launch {
            userPreferencesRepository.setDisplayFeatureDiscoverBanner(
                AliasManagementOptions,
                FeatureDiscoveryBannerPreference.NotDisplay
            )
        }
    }

    private fun onFieldRemoved(event: DraftFormFieldEvent.FieldRemoved) {
        val (_, index) = event
        aliasItemFormMutableState = aliasItemFormState.copy(
            customFields = aliasItemFormState.customFields
                .toMutableList()
                .apply { removeAt(index) }
                .toPersistentList()
        )
    }

    private fun onFieldRenamed(event: DraftFormFieldEvent.FieldRenamed) {
        val (_, index, newLabel) = event
        val updated = customFieldHandler.onCustomFieldRenamed(
            customFieldList = aliasItemFormState.customFields,
            index = index,
            newLabel = newLabel
        )
        aliasItemFormMutableState = aliasItemFormState.copy(customFields = updated)
    }

    private fun onFieldAdded(event: DraftFormFieldEvent.FieldAdded) {
        val (_, label, type) = event
        val added = customFieldHandler.onCustomFieldAdded(label, type)
        aliasItemFormMutableState = aliasItemFormState.copy(
            customFields = aliasItemFormState.customFields + added
        )
        val identifier = CustomFieldIdentifier(
            index = aliasItemFormState.customFields.lastIndex,
            type = type
        )
        focusedFieldState.update { AliasField.CustomField(identifier).some() }
    }

    private fun removeValidationErrors(vararg errors: ValidationError) {
        aliasItemValidationErrorsState.update { currentValidationErrors ->
            currentValidationErrors.toMutableSet().apply {
                errors.forEach { error -> remove(error) }
            }
        }
    }

    internal fun onCustomFieldChange(id: CustomFieldIdentifier, value: String) {
        removeValidationErrors(
            CustomFieldValidationError.EmptyField(index = id.index),
            CustomFieldValidationError.InvalidTotp(index = id.index)
        )
        val updated = customFieldHandler.onCustomFieldValueChanged(
            customFieldIdentifier = id,
            customFieldList = aliasItemFormState.customFields,
            value = value
        )
        aliasItemFormMutableState = aliasItemFormState.copy(
            customFields = updated.toPersistentList()
        )
    }

    internal fun onFocusChange(field: AliasField.CustomField, isFocused: Boolean) {
        val customFields = customFieldHandler.onCustomFieldFocusedChanged(
            customFieldIdentifier = field.field,
            customFieldList = aliasItemFormState.customFields,
            isFocused = isFocused
        )
        aliasItemFormMutableState = aliasItemFormState.copy(customFields = customFields)
        if (isFocused) {
            focusedFieldState.update { field.some() }
        } else {
            focusedFieldState.update { None }
        }
    }

    protected suspend fun isFormStateValid(originalCustomFields: List<UICustomFieldContent> = emptyList()): Boolean {
        val result = encryptionContextProvider.withEncryptionContextSuspendable {
            aliasItemFormProcessor.process(
                AliasItemFormProcessor.Input(
                    formState = aliasItemFormState,
                    originalCustomFields = originalCustomFields
                ),
                ::decrypt,
                ::encrypt
            )
        }
        return when (result) {
            is FormProcessingResult.Error -> {
                aliasItemValidationErrorsState.update { result.errors }
                false
            }
            is FormProcessingResult.Success -> {
                aliasItemFormMutableState = result.sanitized
                true
            }
        }
    }
}
