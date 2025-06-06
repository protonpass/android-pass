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

package proton.android.pass.features.itemcreate.identity.presentation.bottomsheets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.repositories.DRAFT_IDENTITY_CUSTOM_FIELD_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.features.itemcreate.identity.navigation.bottomsheets.AddIdentityFieldType
import proton.android.pass.features.itemcreate.identity.navigation.bottomsheets.IdentityFieldsSectionNavArgId
import proton.android.pass.features.itemcreate.identity.navigation.bottomsheets.IdentitySectionIndexNavArgId
import proton.android.pass.features.itemcreate.identity.presentation.IdentityField
import proton.android.pass.features.itemcreate.identity.ui.IdentitySectionType
import javax.inject.Inject

@HiltViewModel
class IdentityFieldsViewModel @Inject constructor(
    private val identityFieldDraftRepository: IdentityFieldDraftRepository,
    private val draftRepository: DraftRepository,
    canPerformPaidAction: CanPerformPaidAction,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val addIdentityFieldType: AddIdentityFieldType =
        savedStateHandleProvider.get().require(IdentityFieldsSectionNavArgId.key)
    private val sectionIndex: Int =
        savedStateHandleProvider.get().require(IdentitySectionIndexNavArgId.key)

    private val eventFlow: MutableStateFlow<IdentityFieldsEvent> =
        MutableStateFlow(IdentityFieldsEvent.Idle)

    private val fieldFlow: Flow<PersistentSet<IdentityField>> = combine(
        identityFieldDraftRepository.observeSectionFields(
            addIdentityFieldType.toSection(sectionIndex), sectionIndex
        ),
        canPerformPaidAction()
    ) { sectionFields: Set<IdentityField>, isPaidPlan: Boolean ->
        if (isPaidPlan) {
            sectionFields
        } else {
            sectionFields.filterNot { it is IdentityField.CustomField }
        }.toPersistentSet()
    }

    val state = combine(fieldFlow, eventFlow, ::IdentityFieldsUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = IdentityFieldsUiState.Initial
        )

    fun onFieldClick(extraField: IdentityField) {
        identityFieldDraftRepository.addField(extraField, true)
        if (extraField is IdentityField.CustomField) {
            draftRepository.save(DRAFT_IDENTITY_CUSTOM_FIELD_KEY, extraField)
            eventFlow.update { IdentityFieldsEvent.OnAddCustomExtraField }
        } else {
            eventFlow.update { IdentityFieldsEvent.OnAddExtraField }
        }
    }

    fun consumeEvent(event: IdentityFieldsEvent) = viewModelScope.launch {
        eventFlow.compareAndSet(event, IdentityFieldsEvent.Idle)
    }
}

fun AddIdentityFieldType.toSection(sectionIndex: Int): IdentitySectionType = when (this) {
    AddIdentityFieldType.Personal -> IdentitySectionType.PersonalDetails
    AddIdentityFieldType.Contact -> IdentitySectionType.ContactDetails
    AddIdentityFieldType.Address -> IdentitySectionType.AddressDetails
    AddIdentityFieldType.Work -> IdentitySectionType.WorkDetails
    AddIdentityFieldType.Extra -> IdentitySectionType.ExtraSection(sectionIndex)
}

