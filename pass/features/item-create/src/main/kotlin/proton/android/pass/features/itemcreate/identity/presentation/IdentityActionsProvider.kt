/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.itemcreate.identity.presentation

import android.content.Context
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.Item
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIExtraSection
import proton.android.pass.features.itemcreate.common.ValidationError
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Birthdate
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.County
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.CustomExtraField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.ExtraField
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Facebook
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.FirstName
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Floor
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Gender
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Instagram
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.LastName
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Linkedin
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.MiddleName
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.PersonalWebsite
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Reddit
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.WorkEmail
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.WorkPhoneNumber
import proton.android.pass.features.itemcreate.identity.presentation.bottomsheets.Yahoo
import java.net.URI

@Suppress("ComplexInterface", "TooManyFunctions")
interface IdentityFormActions {
    fun onFieldChange(field: IdentityField, value: String)
    fun onFocusChange(field: IdentityField, isFocused: Boolean)
    fun observeActions(coroutineScope: CoroutineScope)
    fun getFormState(): IdentityItemFormState
    suspend fun isFormStateValid(
        originalPersonalCustomFields: List<UICustomFieldContent> = emptyList(),
        originalAddressCustomFields: List<UICustomFieldContent> = emptyList(),
        originalContactCustomFields: List<UICustomFieldContent> = emptyList(),
        originalWorkCustomFields: List<UICustomFieldContent> = emptyList(),
        originalSections: List<UIExtraSection> = emptyList()
    ): Boolean
    fun clearDraftData()

    fun openDraftAttachment(
        contextHolder: ClassHolder<Context>,
        uri: URI,
        mimetype: String
    )

    suspend fun openAttachment(contextHolder: ClassHolder<Context>, attachment: Attachment)
    suspend fun retryUploadDraftAttachment(metadata: FileMetadata)
    suspend fun dismissFileAttachmentsOnboardingBanner()
    suspend fun pasteTotp()

    fun onSocialSecurityNumberFieldFocusChange(isFocused: Boolean)
}

interface IdentityActionsProvider : IdentityFormActions {
    fun observeSharedState(): Flow<IdentitySharedUiState>
    fun updateLoadingState(loadingState: IsLoadingState)
    suspend fun onItemSavedState(item: Item)
    fun updateSelectedSection(customExtraField: CustomExtraField)
    suspend fun onItemReceivedState(item: Item)
    fun getReceivedItem(): Item
    fun observeReceivedItem(): Flow<Option<Item>>
}

data class IdentitySharedUiState(
    val isLoadingState: IsLoadingState,
    val hasUserEditedContent: Boolean,
    val validationErrors: PersistentSet<ValidationError>,
    val isItemSaved: ItemSavedState,
    val extraFields: PersistentSet<ExtraField>,
    val focusedField: Option<IdentityField>,
    val canUseCustomFields: Boolean,
    val displayFileAttachmentsOnboarding: Boolean,
    val isFileAttachmentsEnabled: Boolean,
    val attachmentsState: AttachmentsState
) {

    val showAddPersonalDetailsButton: Boolean = if (canUseCustomFields) {
        true
    } else {
        !extraFields.containsAll(setOf(FirstName, MiddleName, LastName, Birthdate, Gender))
    }

    val showAddAddressDetailsButton: Boolean = if (canUseCustomFields) {
        true
    } else {
        !extraFields.containsAll(setOf(Floor, County))
    }

    val showAddContactDetailsButton: Boolean = if (canUseCustomFields) {
        true
    } else {
        !extraFields.containsAll(setOf(Linkedin, Reddit, Facebook, Yahoo, Instagram))
    }

    val showAddWorkDetailsButton: Boolean = if (canUseCustomFields) {
        true
    } else {
        !extraFields.containsAll(setOf(PersonalWebsite, WorkPhoneNumber, WorkEmail))
    }

    val showFileAttachments = isFileAttachmentsEnabled
    val showFileAttachmentsBanner = isFileAttachmentsEnabled && displayFileAttachmentsOnboarding

    companion object {
        val Initial = IdentitySharedUiState(
            isLoadingState = IsLoadingState.NotLoading,
            hasUserEditedContent = false,
            validationErrors = persistentSetOf(),
            isItemSaved = ItemSavedState.Unknown,
            extraFields = persistentSetOf(),
            focusedField = None,
            canUseCustomFields = false,
            displayFileAttachmentsOnboarding = false,
            isFileAttachmentsEnabled = false,
            attachmentsState = AttachmentsState.Initial
        )
    }
}
