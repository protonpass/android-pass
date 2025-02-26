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

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.DRAFT_EDIT_CUSTOM_FIELD_TITLE_KEY
import proton.android.pass.data.api.repositories.DRAFT_EDIT_CUSTOM_SECTION_TITLE_KEY
import proton.android.pass.data.api.repositories.DRAFT_IDENTITY_CUSTOM_FIELD_KEY
import proton.android.pass.data.api.repositories.DRAFT_IDENTITY_EXTRA_SECTION_KEY
import proton.android.pass.data.api.repositories.DRAFT_NEW_CUSTOM_FIELD_KEY
import proton.android.pass.data.api.repositories.DRAFT_REMOVE_CUSTOM_FIELD_KEY
import proton.android.pass.data.api.repositories.DRAFT_REMOVE_CUSTOM_SECTION_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.common.CustomFieldIndexTitle
import proton.android.pass.features.itemcreate.common.CustomItemFieldDraftRepository
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIExtraSection
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.attachments.AttachmentsHandler
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.OnCustomFieldChanged
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.BaseCustomItemCommonIntent.OnTitleChanged
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.CustomExtraField

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
    data object ObserveCustomFields : BaseCustomItemCommonIntent
}

abstract class BaseCustomItemViewModel(
    private val draftRepository: DraftRepository,
    private val customItemFieldDraftRepository: CustomItemFieldDraftRepository,
    private val attachmentsHandler: AttachmentsHandler,
    private val encryptionContextProvider: EncryptionContextProvider,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    init {
        processCommonIntent(BaseCustomItemCommonIntent.ObserveCustomFields)
    }

    @OptIn(SavedStateHandleSaveableApi::class)
    var itemFormState: ItemFormState by savedStateHandleProvider.get()
        .saveable { mutableStateOf(ItemFormState.EMPTY) }
        private set

    private val isLoadingState = MutableStateFlow<IsLoadingState>(IsLoadingState.NotLoading)
    private val hasUserEditedContentState = MutableStateFlow(false)
    private val validationErrorsState = MutableStateFlow(emptySet<ItemValidationErrors>())
    private val isItemSavedState = MutableStateFlow<ItemSavedState>(ItemSavedState.Unknown)

    protected fun processCommonIntent(intent: BaseCustomItemCommonIntent) {
        when (intent) {
            BaseCustomItemCommonIntent.ObserveCustomFields -> onObserveCustomFields()
            is OnTitleChanged -> onTitleChange(intent.value)
            is OnCustomFieldChanged ->
                onCustomFieldChange(intent.index, intent.value, intent.sectionIndex)

            BaseCustomItemCommonIntent.ClearDraft -> onClearDraft()
            BaseCustomItemCommonIntent.ClearLastAddedFieldFocus -> onClearLastAddedFieldFocus()
            is BaseCustomItemCommonIntent.OnCustomFieldFocusedChanged ->
                onCustomFieldFocusedChanged(intent.index, intent.value, intent.sectionIndex)

        }
    }

    private fun onObserveCustomFields() {
        viewModelScope.launch { observeNewCustomField() }
        viewModelScope.launch { observeRemoveCustomField() }
        viewModelScope.launch { observeRenameCustomField() }
        viewModelScope.launch { observeNewExtraSection() }
        viewModelScope.launch { observeRemoveExtraSection() }
        viewModelScope.launch { observeRenameExtraSection() }
        // observeNewAttachments(coroutineScope)
        // observeHasDeletedAttachments(coroutineScope)
        // observeHasRenamedAttachments(coroutineScope)
    }

    private suspend fun observeNewCustomField() {
        draftRepository.get<CustomFieldContent>(DRAFT_NEW_CUSTOM_FIELD_KEY)
            .collect {
                if (it !is Some) return@collect
                draftRepository.delete<CustomFieldContent>(DRAFT_NEW_CUSTOM_FIELD_KEY)
                val extraFieldType =
                    draftRepository.delete<CustomExtraField>(DRAFT_IDENTITY_CUSTOM_FIELD_KEY)
                if (extraFieldType !is Some) return@collect
                TODO()
                // onAddCustomField(it.value, extraFieldType.value)
            }
    }

    private suspend fun observeNewExtraSection() {
        draftRepository.get<String>(DRAFT_IDENTITY_EXTRA_SECTION_KEY)
            .collect {
                if (it !is Some) return@collect
                draftRepository.delete<String>(DRAFT_IDENTITY_EXTRA_SECTION_KEY)
                onAddExtraSection(it.value)
            }
    }

    private fun onAddExtraSection(value: String) {
        itemFormState = itemFormState.copy(
            sectionList = itemFormState.sectionList + listOf(UIExtraSection(value, emptyList()))
        )
    }

    private suspend fun observeRemoveCustomField() {
        draftRepository.get<Int>(DRAFT_REMOVE_CUSTOM_FIELD_KEY)
            .collect {
                if (it !is Some) return@collect
                draftRepository.delete<Int>(DRAFT_REMOVE_CUSTOM_FIELD_KEY)
                val extraFieldType =
                    draftRepository.delete<CustomExtraField>(DRAFT_IDENTITY_CUSTOM_FIELD_KEY)
                if (extraFieldType !is Some) return@collect
                TODO()
                // onRemoveCustomField(it.value, extraFieldType.value)
            }
    }

    private suspend fun observeRemoveExtraSection() {
        draftRepository.get<Int>(DRAFT_REMOVE_CUSTOM_SECTION_KEY)
            .collect {
                if (it !is Some) return@collect
                draftRepository.delete<Int>(DRAFT_REMOVE_CUSTOM_SECTION_KEY)
                onRemoveCustomSection(it.value)
            }
    }

    private fun onRemoveCustomSection(index: Int) {
        itemFormState = itemFormState.copy(
            sectionList = itemFormState.sectionList.toMutableList().apply { removeAt(index) }
        )
    }

    private suspend fun observeRenameCustomField() {
        draftRepository.get<CustomFieldIndexTitle>(DRAFT_EDIT_CUSTOM_FIELD_TITLE_KEY)
            .collect {
                if (it !is Some) return@collect
                draftRepository.delete<CustomFieldIndexTitle>(DRAFT_EDIT_CUSTOM_FIELD_TITLE_KEY)
                val extraFieldType =
                    draftRepository.delete<CustomExtraField>(DRAFT_IDENTITY_CUSTOM_FIELD_KEY)
                if (extraFieldType !is Some) return@collect
                TODO()
                // onRenameCustomField(it.value, extraFieldType.value)
            }
    }

    private suspend fun observeRenameExtraSection() {
        draftRepository.get<CustomFieldIndexTitle>(DRAFT_EDIT_CUSTOM_SECTION_TITLE_KEY)
            .collect {
                if (it !is Some) return@collect
                draftRepository.delete<CustomFieldIndexTitle>(DRAFT_EDIT_CUSTOM_SECTION_TITLE_KEY)
                onRenameCustomSection(it.value)
            }
    }

    private fun onRenameCustomSection(value: CustomFieldIndexTitle) {
        itemFormState = itemFormState.copy(
            sectionList = itemFormState.sectionList.toMutableList()
                .apply {
                    set(
                        value.index,
                        itemFormState.sectionList[value.index].copy(title = value.title)
                    )
                }
        )
    }

    private fun onCustomFieldFocusedChanged(
        fieldIndex: Int,
        isFocused: Boolean,
        sectionIndex: Option<Int>
    ) {
        val sectionPos = sectionIndex.value() ?: 0
        if (sectionPos >= itemFormState.sectionList.size) return

        itemFormState = itemFormState.copy(
            sectionList = itemFormState.sectionList.mapIndexed sections@{ index, section ->
                if (index != fieldIndex) return@sections section

                val updatedFields = section.customFields.mapIndexed fields@{ i, field ->
                    if (i != fieldIndex || field !is UICustomFieldContent.Hidden) return@fields field

                    when {
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
                }

                section.copy(customFields = updatedFields)
            }
        )
    }

    private fun onClearLastAddedFieldFocus() {
        customItemFieldDraftRepository.resetLastAddedCustomField()
    }

    private fun onClearDraft() {
        customItemFieldDraftRepository.clearAddedFields()
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
        value: String
    ): List<UICustomFieldContent> = fields.toMutableList().apply {
        if (index in indices) {
            val updatedField = when (val field = get(index)) {
                is UICustomFieldContent.Hidden -> field.copy(value)
                is UICustomFieldContent.Text -> field.copy(value)
                is UICustomFieldContent.Totp -> field.copy(value)
            }
            set(index, updatedField)
        }
    }

    fun observeSharedState(): Flow<ItemSharedUiState> = combine(
        isLoadingState,
        hasUserEditedContentState,
        validationErrorsState,
        isItemSavedState
    ) { isLoading, hasEdited, errors, savedState ->
        ItemSharedUiState(
            isLoadingState = isLoading,
            hasUserEditedContent = hasEdited,
            validationErrors = errors.toPersistentSet(),
            isItemSaved = savedState,
            focusedField = None,
            canUseCustomFields = true,
            displayFileAttachmentsOnboarding = false,
            isFileAttachmentsEnabled = true,
            attachmentsState = AttachmentsState.Initial
        )
    }
}
