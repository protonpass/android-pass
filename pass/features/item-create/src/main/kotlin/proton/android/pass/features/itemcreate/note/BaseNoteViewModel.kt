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

package proton.android.pass.features.itemcreate.note

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
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.common.CommonFieldValidationError
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.CustomFieldValidationError
import proton.android.pass.features.itemcreate.common.DraftFormFieldEvent
import proton.android.pass.features.itemcreate.common.ValidationError
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandler
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldIdentifier
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.DisplayFileAttachmentsBanner.NotDisplay
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import java.net.URI

abstract class BaseNoteViewModel(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val attachmentsHandler: AttachmentsHandler,
    private val featureFlagsRepository: FeatureFlagsPreferencesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val customFieldHandler: CustomFieldHandler,
    getUserPlan: GetUserPlan,
    customFieldDraftRepository: CustomFieldDraftRepository,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val hasUserEditedContentFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

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

    @OptIn(SavedStateHandleSaveableApi::class)
    protected var noteItemFormMutableState: NoteItemFormState by savedStateHandleProvider.get()
        .saveable { mutableStateOf(NoteItemFormState.Empty) }
    val noteItemFormState: NoteItemFormState get() = noteItemFormMutableState

    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    protected val noteItemValidationErrorsState: MutableStateFlow<Set<ValidationError>> =
        MutableStateFlow(emptySet())
    private val focusedFieldState: MutableStateFlow<Option<NoteField>> =
        MutableStateFlow<Option<NoteField>>(None)

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    internal val baseNoteUiState: StateFlow<BaseNoteUiState> = combineN(
        noteItemValidationErrorsState,
        isLoadingState,
        isItemSavedState,
        hasUserEditedContentFlow,
        attachmentsHandler.attachmentState,
        featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1),
        featureFlagsRepository.get<Boolean>(FeatureFlag.CUSTOM_TYPE_V1),
        userPreferencesRepository.observeDisplayFileAttachmentsOnboarding(),
        getUserPlan(),
        focusedFieldState
    ) { noteItemValidationErrors, isLoading, isItemSaved, hasUserEditedContent, attachmentsState,
        isFileAttachmentsEnabled, isCustomItemEnabled, displayFileAttachmentsOnboarding,
        userPlan, focusedField ->
        BaseNoteUiState(
            errorList = noteItemValidationErrors,
            isLoadingState = isLoading,
            itemSavedState = isItemSaved,
            hasUserEditedContent = hasUserEditedContent,
            attachmentsState = attachmentsState,
            displayFileAttachmentsOnboarding = displayFileAttachmentsOnboarding.value(),
            isFileAttachmentsEnabled = isFileAttachmentsEnabled,
            isCustomItemEnabled = isCustomItemEnabled,
            canUseCustomFields = userPlan.hasPlanWithAccess,
            focusedField = focusedField.value()
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BaseNoteUiState.Initial
        )

    fun onTitleChange(value: String) {
        onUserEditedContent()
        noteItemFormMutableState = noteItemFormMutableState.copy(title = value)
        noteItemValidationErrorsState.update {
            it.toMutableSet().apply { remove(CommonFieldValidationError.BlankTitle) }
        }
    }

    fun onNoteChange(value: String) {
        onUserEditedContent()
        noteItemFormMutableState = noteItemFormMutableState.copy(note = value)
    }

    protected fun onUserEditedContent() {
        if (hasUserEditedContentFlow.value) return
        hasUserEditedContentFlow.update { true }
    }

    fun onEmitSnackbarMessage(snackbarMessage: NoteSnackbarMessage) = viewModelScope.launch {
        snackbarDispatcher(snackbarMessage)
    }

    fun openDraftAttachment(
        contextHolder: ClassHolder<Context>,
        uri: URI,
        mimetype: String
    ) {
        attachmentsHandler.openDraftAttachment(contextHolder, uri, mimetype)
    }

    fun onAttachmentOpen(contextHolder: ClassHolder<Context>, attachment: Attachment) {
        viewModelScope.launch {
            attachmentsHandler.openAttachment(contextHolder, attachment)
        }
    }

    internal fun clearDraftData() {
        attachmentsHandler.onClearAttachments()
    }

    fun openAttachment(contextHolder: ClassHolder<Context>, attachment: Attachment) {
        viewModelScope.launch {
            attachmentsHandler.openAttachment(
                contextHolder = contextHolder,
                attachment = attachment
            )
        }
    }

    private fun onFieldRemoved(event: DraftFormFieldEvent.FieldRemoved) {
        val (_, index) = event
        noteItemFormMutableState = noteItemFormState.copy(
            customFields = noteItemFormState.customFields
                .toMutableList()
                .apply { removeAt(index) }
                .toPersistentList()
        )
    }

    private fun onFieldRenamed(event: DraftFormFieldEvent.FieldRenamed) {
        val (_, index, newLabel) = event
        val updated = customFieldHandler.onCustomFieldRenamed(
            customFieldList = noteItemFormState.customFields,
            index = index,
            newLabel = newLabel
        )
        noteItemFormMutableState = noteItemFormState.copy(customFields = updated)
    }

    private fun onFieldAdded(event: DraftFormFieldEvent.FieldAdded) {
        val (_, label, type) = event
        val added = customFieldHandler.onCustomFieldAdded(label, type)
        noteItemFormMutableState = noteItemFormState.copy(
            customFields = noteItemFormState.customFields + added
        )
        val identifier = CustomFieldIdentifier(
            index = noteItemFormState.customFields.lastIndex,
            type = type
        )
        focusedFieldState.update { NoteField.CustomField(identifier).some() }
    }

    internal fun onCustomFieldChange(id: CustomFieldIdentifier, value: String) {
        removeValidationErrors(
            CustomFieldValidationError.EmptyField(index = id.index),
            CustomFieldValidationError.InvalidTotp(index = id.index)
        )
        val updated = customFieldHandler.onCustomFieldValueChanged(
            customFieldIdentifier = id,
            customFieldList = noteItemFormState.customFields,
            value = value
        )
        noteItemFormMutableState = noteItemFormState.copy(
            customFields = updated.toPersistentList()
        )
    }

    internal fun onFocusChange(field: NoteField.CustomField, isFocused: Boolean) {
        val customFields = customFieldHandler.onCustomFieldFocusedChanged(
            customFieldIdentifier = field.field,
            customFieldList = noteItemFormState.customFields,
            isFocused = isFocused
        )
        noteItemFormMutableState = noteItemFormState.copy(customFields = customFields)
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
            userPreferencesRepository.setDisplayFileAttachmentsOnboarding(NotDisplay)
        }
    }

    private fun removeValidationErrors(vararg errors: ValidationError) {
        noteItemValidationErrorsState.update { currentValidationErrors ->
            currentValidationErrors.toMutableSet().apply {
                errors.forEach { error -> remove(error) }
            }
        }
    }
}
