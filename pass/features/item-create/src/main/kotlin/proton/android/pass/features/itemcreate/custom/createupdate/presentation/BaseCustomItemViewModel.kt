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
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.attachments.LinkAttachmentsToItem
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.DraftFormFieldEvent
import proton.android.pass.features.itemcreate.common.DraftFormSectionEvent
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UICustomFieldContent.Companion.createCustomField
import proton.android.pass.features.itemcreate.common.UIExtraSection
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.attachments.AttachmentsHandler
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.OnCustomFieldChanged
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.OnTitleChanged
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.DisplayFileAttachmentsBanner.NotDisplay
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import java.net.URI

sealed interface BaseItemFormIntent
sealed interface BaseCustomItemCommonIntent : BaseItemFormIntent {
    @JvmInline
    value class OnTitleChanged(val value: String) : BaseCustomItemCommonIntent

    data class OnCustomFieldChanged(
        val index: Int,
        val value: String,
        val sectionIndex: Option<Int>
    ) : BaseCustomItemCommonIntent

    data class OnCustomFieldFocusedChanged(
        val index: Int,
        val value: Boolean,
        val sectionIndex: Option<Int>
    ) : BaseCustomItemCommonIntent

    data object ClearDraft : BaseCustomItemCommonIntent
    data object ClearLastAddedFieldFocus : BaseCustomItemCommonIntent
    data object ViewModelObserve : BaseCustomItemCommonIntent

    data class OnOpenDraftAttachment(
        val contextHolder: ClassHolder<Context>,
        val uri: URI,
        val mimetype: String
    ) : BaseCustomItemCommonIntent

    @JvmInline
    value class OnRetryUploadAttachment(val metadata: FileMetadata) : BaseCustomItemCommonIntent

    data object DismissFileAttachmentsBanner : BaseCustomItemCommonIntent
}

abstract class BaseCustomItemViewModel(
    private val linkAttachmentsToItem: LinkAttachmentsToItem,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val customFieldDraftRepository: CustomFieldDraftRepository,
    private val attachmentsHandler: AttachmentsHandler,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val featureFlagsRepository: FeatureFlagsPreferencesRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    init {
        processCommonIntent(BaseCustomItemCommonIntent.ViewModelObserve)
    }

    @OptIn(SavedStateHandleSaveableApi::class)
    var itemFormState: ItemFormState by savedStateHandleProvider.get()
        .saveable { mutableStateOf(ItemFormState.EMPTY) }
        protected set

    private val isLoadingState = MutableStateFlow<IsLoadingState>(IsLoadingState.NotLoading)
    private val hasUserEditedContentState = MutableStateFlow(false)
    private val validationErrorsState = MutableStateFlow(emptySet<ItemValidationErrors>())
    private val isItemSavedState = MutableStateFlow<ItemSavedState>(ItemSavedState.Unknown)
    private val focusedFieldState = MutableStateFlow<Option<FocusedField>>(None)

    protected fun processCommonIntent(intent: BaseCustomItemCommonIntent) {
        when (intent) {
            BaseCustomItemCommonIntent.ViewModelObserve -> onViewModelObserve()
            is OnTitleChanged -> onTitleChange(intent.value)
            is OnCustomFieldChanged ->
                onCustomFieldChange(intent.index, intent.value, intent.sectionIndex)

            BaseCustomItemCommonIntent.ClearDraft -> onClearDraft()
            BaseCustomItemCommonIntent.ClearLastAddedFieldFocus -> onClearLastAddedFieldFocus()
            is BaseCustomItemCommonIntent.OnCustomFieldFocusedChanged ->
                onCustomFieldFocusedChanged(intent.index, intent.value, intent.sectionIndex)

            BaseCustomItemCommonIntent.DismissFileAttachmentsBanner ->
                dismissFileAttachmentsOnboardingBanner()

            is BaseCustomItemCommonIntent.OnOpenDraftAttachment ->
                openDraftAttachment(intent.contextHolder, intent.uri, intent.mimetype)

            is BaseCustomItemCommonIntent.OnRetryUploadAttachment ->
                retryUploadDraftAttachment(intent.metadata)
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
                val updatedSection = section.copy(
                    customFields = section.customFields.toMutableList().apply {
                        set(index, section.customFields[index].updateLabel(newLabel))
                    }
                )
                itemFormState = itemFormState.copy(
                    sectionList = itemFormState.sectionList.toMutableList().apply {
                        set(sectionIndex.value, updatedSection)
                    }
                )
            }

            is None -> {
                itemFormState = itemFormState.copy(
                    customFieldList = itemFormState.customFieldList.toMutableList().apply {
                        set(index, itemFormState.customFieldList[index].updateLabel(newLabel))
                    }
                )
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
        val field = encryptionContextProvider.withEncryptionContext {
            createCustomField(type, label, this)
        }
        when (sectionIndex) {
            is Some -> {
                val section = itemFormState.sectionList[sectionIndex.value]
                val updatedSection = section.copy(
                    customFields = section.customFields + field
                )
                itemFormState = itemFormState.copy(
                    sectionList = itemFormState.sectionList.toMutableList().apply {
                        set(sectionIndex.value, updatedSection)
                    }
                )
                focusedFieldState.update {
                    FocusedField(
                        sectionIndex = sectionIndex,
                        index = section.customFields.lastIndex
                    ).some()
                }
            }

            is None -> {
                itemFormState = itemFormState.copy(
                    customFieldList = itemFormState.customFieldList + field
                )
                focusedFieldState.update {
                    FocusedField(
                        sectionIndex = sectionIndex,
                        index = itemFormState.customFieldList.lastIndex
                    ).some()
                }
            }
        }
    }

    private fun onCustomFieldFocusedChanged(
        focusedFieldIndex: Int,
        isFocused: Boolean,
        sectionIndex: Option<Int>
    ) {
        when (sectionIndex) {
            None -> itemFormState = itemFormState.copy(
                customFieldList = itemFormState.customFieldList.mapIndexed fields@{ customFieldIndex, field ->
                    updateFocus(customFieldIndex, focusedFieldIndex, field, isFocused)
                }
            )

            is Some -> {
                val sectionPos = sectionIndex.value() ?: 0
                if (sectionPos >= itemFormState.sectionList.size) return

                itemFormState = itemFormState.copy(
                    sectionList = itemFormState.sectionList.mapIndexed sections@{ index, section ->
                        if (index != focusedFieldIndex) return@sections section

                        val updatedFields = section.customFields.mapIndexed fields@{ customFieldIndex, field ->
                            updateFocus(customFieldIndex, focusedFieldIndex, field, isFocused)
                        }

                        section.copy(customFields = updatedFields)
                    }
                )
            }
        }
    }

    private fun updateFocus(
        customFieldIndex: Int,
        focusedFieldIndex: Int,
        field: UICustomFieldContent,
        isFocused: Boolean
    ) = when {
        customFieldIndex != focusedFieldIndex || field !is UICustomFieldContent.Hidden -> field
        field.value is UIHiddenState.Empty -> field
        isFocused -> encryptionContextProvider.withEncryptionContext {
            field.copy(
                value = UIHiddenState.Revealed(
                    encrypted = field.value.encrypted,
                    clearText = decrypt(field.value.encrypted)
                )
            )
        }

        else -> field.copy(
            value = UIHiddenState.Concealed(encrypted = field.value.encrypted)
        )
    }

    private fun onClearLastAddedFieldFocus() {
        focusedFieldState.update { None }
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

    protected fun isFormStateValid(): Boolean {
        val validationErrors = itemFormState.validate()
        validationErrorsState.update { validationErrors }
        return validationErrors.isEmpty()
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

    private fun onCustomFieldChange(
        index: Int,
        value: String,
        sectionIndex: Option<Int>
    ) {
        onUserEditedContent()
        when (sectionIndex) {
            None -> {
                val updatedFields = updateCustomField(itemFormState.customFieldList, index, value)
                itemFormState = itemFormState.copy(customFieldList = updatedFields)
            }

            is Some -> {
                val currentFields = itemFormState.sectionList[sectionIndex.value].customFields
                val updatedFields = updateCustomField(currentFields, index, value)
                val updatedSections = itemFormState.sectionList.toMutableList().apply {
                    set(
                        sectionIndex.value,
                        itemFormState.sectionList[sectionIndex.value]
                            .copy(customFields = updatedFields)
                    )
                }
                itemFormState = itemFormState.copy(sectionList = updatedSections)
            }
        }
    }

    private fun updateCustomField(
        fields: List<UICustomFieldContent>,
        index: Int,
        newValue: String
    ): List<UICustomFieldContent> = fields.toMutableList().apply {
        if (index in indices) {
            val updatedField = encryptionContextProvider.withEncryptionContext {
                when (val content = get(index)) {
                    is UICustomFieldContent.Hidden -> {
                        UICustomFieldContent.Hidden(
                            label = content.label,
                            value = if (newValue.isBlank()) {
                                UIHiddenState.Empty(encrypt(""))
                            } else {
                                UIHiddenState.Revealed(
                                    encrypted = encrypt(newValue),
                                    clearText = newValue
                                )
                            }
                        )
                    }

                    is UICustomFieldContent.Text -> UICustomFieldContent.Text(
                        label = content.label,
                        value = newValue
                    )

                    is UICustomFieldContent.Totp -> UICustomFieldContent.Totp(
                        label = content.label,
                        value = UIHiddenState.Revealed(
                            encrypted = encrypt(newValue),
                            clearText = newValue
                        ),
                        id = content.id
                    )
                }
            }
            set(index, updatedField)
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

    protected fun observeSharedState(): Flow<ItemSharedUiState> = combineN(
        isLoadingState,
        hasUserEditedContentState,
        validationErrorsState,
        isItemSavedState,
        focusedFieldState,
        featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1),
        userPreferencesRepository.observeDisplayFileAttachmentsOnboarding(),
        attachmentsHandler.attachmentState
    ) { isLoading, hasEdited, errors, savedState, lastAddedField,
        isFileAttachmentsEnabled, displayFileAttachmentsOnboarding, attachmentsState ->
        ItemSharedUiState(
            isLoadingState = isLoading,
            hasUserEditedContent = hasEdited,
            validationErrors = errors.toPersistentSet(),
            isItemSaved = savedState,
            focusedField = lastAddedField,
            canUseCustomFields = true,
            displayFileAttachmentsOnboarding = displayFileAttachmentsOnboarding.value(),
            isFileAttachmentsEnabled = isFileAttachmentsEnabled,
            attachmentsState = attachmentsState
        )
    }

    companion object {
        private const val TAG = "BaseCustomItemViewModel"
    }
}
