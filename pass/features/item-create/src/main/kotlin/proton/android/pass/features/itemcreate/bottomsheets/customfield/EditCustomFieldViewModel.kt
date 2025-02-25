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

package proton.android.pass.features.itemcreate.bottomsheets.customfield

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.repositories.DRAFT_REMOVE_CUSTOM_FIELD_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.features.itemcreate.common.customsection.CustomSectionIndexNavArgId
import proton.android.pass.navigation.api.NavParamEncoder
import javax.inject.Inject

@HiltViewModel
class EditCustomFieldViewModel @Inject constructor(
    private val draftRepository: DraftRepository,
    savedStateHandle: SavedStateHandleProvider
) : ViewModel() {

    private val index = savedStateHandle.get().require<Int>(CustomFieldIndexNavArgId.key)
    private val title = savedStateHandle.get().require<String>(CustomFieldTitleNavArgId.key).let {
        NavParamEncoder.decode(it)
    }
    private val sectionIndex = savedStateHandle.get().require<Int>(CustomSectionIndexNavArgId.key)
        .let { if (it > 0) Some(it) else None }


    private val eventStateFlow: MutableStateFlow<EditCustomFieldEvent> =
        MutableStateFlow(EditCustomFieldEvent.Unknown)

    val eventState: StateFlow<EditCustomFieldEvent> = eventStateFlow

    fun onEdit() {
        eventStateFlow.update {
            EditCustomFieldEvent.EditField(
                index = index,
                title = title,
                sectionIndex = sectionIndex
            )
        }
    }

    fun onRemove() {
        draftRepository.save(DRAFT_REMOVE_CUSTOM_FIELD_KEY, index)
        eventStateFlow.update { EditCustomFieldEvent.RemovedField }
    }
}

sealed interface EditCustomFieldEvent {
    data object Unknown : EditCustomFieldEvent
    data class EditField(
        val index: Int,
        val title: String,
        val sectionIndex: Option<Int>
    ) : EditCustomFieldEvent

    data object RemovedField : EditCustomFieldEvent
}
