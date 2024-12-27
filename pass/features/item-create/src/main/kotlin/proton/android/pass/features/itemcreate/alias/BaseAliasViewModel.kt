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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.common.attachments.AttachmentsHandler
import proton.android.pass.navigation.api.AliasOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import java.net.URI

abstract class BaseAliasViewModel(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val attachmentsHandler: AttachmentsHandler,
    private val featureFlagsRepository: FeatureFlagsPreferencesRepository,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val hasUserEditedContentFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        attachmentsHandler.observeNewAttachments { newUris ->
            if (newUris.isNotEmpty()) {
                onUserEditedContent()
                newUris.forEach { uri ->
                    viewModelScope.launch {
                        isLoadingState.update { IsLoadingState.Loading }
                        attachmentsHandler.uploadNewAttachment(uri)
                        isLoadingState.update { IsLoadingState.NotLoading }
                    }
                }
            }
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

    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.Loading)
    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    protected val isAliasDraftSavedState: MutableStateFlow<AliasDraftSavedState> =
        MutableStateFlow(AliasDraftSavedState.Unknown)
    protected val aliasItemValidationErrorsState: MutableStateFlow<Set<AliasItemValidationErrors>> =
        MutableStateFlow(emptySet())
    protected val isApplyButtonEnabledState: MutableStateFlow<IsButtonEnabled> =
        MutableStateFlow(IsButtonEnabled.Disabled)
    protected val mutableCloseScreenEventFlow: MutableStateFlow<CloseScreenEvent> =
        MutableStateFlow(CloseScreenEvent.NotClose)
    protected val selectedMailboxListState: MutableStateFlow<List<Int>> =
        MutableStateFlow(emptyList())

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
    val baseAliasUiState: StateFlow<BaseAliasUiState> = combineN(
        aliasItemValidationErrorsState,
        isLoadingState,
        eventWrapperState,
        hasUserEditedContentFlow,
        featureFlagsRepository.get<Boolean>(FeatureFlag.ADVANCED_ALIAS_MANAGEMENT_V1),
        featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1),
        attachmentsHandler.attachmentState
    ) { aliasItemValidationErrors, isLoading, eventWrapper, hasUserEditedContent,
        isAliasManagementEnabled, isFileAttachmentEnabled, attachmentsState ->
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
            isAliasManagementEnabled = isAliasManagementEnabled,
            isFileAttachmentEnabled = isFileAttachmentEnabled,
            attachmentsState = attachmentsState
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

    open fun onMailboxesChanged(mailboxes: List<SelectedAliasMailboxUiModel>) {
        onUserEditedContent()
        val atLeastOneSelected = mailboxes.any { it.selected }
        if (!atLeastOneSelected) return
        selectedMailboxListState.update { mailboxes.filter { it.selected }.map { it.model.id } }
    }

    protected fun getMailboxTitle(mailboxes: List<SelectedAliasMailboxUiModel>): String {
        val allSelectedMailboxes = mailboxes.filter { it.selected }
        if (allSelectedMailboxes.isEmpty()) return ""
        val mailboxTitle = buildString {
            allSelectedMailboxes.forEachIndexed { idx, mailbox ->
                if (idx > 0) append(",\n")
                append(mailbox.model.email)
            }
        }

        return mailboxTitle
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

    override fun onCleared() {
        attachmentsHandler.onClearAttachments()
        super.onCleared()
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
}
