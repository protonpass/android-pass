/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.itemcreate.identity.presentation.customsection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.features.itemcreate.bottomsheets.customfield.CustomFieldTitleNavArgId
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.DraftFormSectionEvent
import proton.android.pass.features.itemcreate.common.customsection.CustomSectionIndexNavArgId
import proton.android.pass.navigation.api.NavParamEncoder
import javax.inject.Inject

@HiltViewModel
class EditCustomSectionNameViewModel @Inject constructor(
    private val customFieldDraftRepository: CustomFieldDraftRepository,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val customSectionIndex: Int = savedStateHandleProvider
        .get()
        .require(CustomSectionIndexNavArgId.key)

    private val customSectionTitle: String = savedStateHandleProvider
        .get()
        .require<String>(CustomFieldTitleNavArgId.key)
        .let(NavParamEncoder::decode)

    private val eventFlow: MutableStateFlow<CustomSectionEvent> =
        MutableStateFlow(CustomSectionEvent.Idle)
    private val nameFlow: MutableStateFlow<String> = MutableStateFlow(customSectionTitle)

    val state: StateFlow<CustomSectionNameUiState> = combine(
        eventFlow,
        nameFlow
    ) { event, value ->
        CustomSectionNameUiState(
            value = value,
            canConfirm = value.isNotBlank(),
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = CustomSectionNameUiState.Initial
    )

    fun onNameChanged(name: String) {
        if (name.contains("\n")) return
        nameFlow.update { name }
    }

    fun onSave() {
        viewModelScope.launch {
            val event = DraftFormSectionEvent.SectionRenamed(customSectionIndex, nameFlow.value.trim())
            customFieldDraftRepository.emit(event)
            eventFlow.update { CustomSectionEvent.Close }
        }
    }
}
