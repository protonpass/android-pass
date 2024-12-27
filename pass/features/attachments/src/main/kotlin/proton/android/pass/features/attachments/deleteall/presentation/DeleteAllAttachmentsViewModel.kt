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

package proton.android.pass.features.attachments.deleteall.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import javax.inject.Inject

@HiltViewModel
class DeleteAllAttachmentsViewModel @Inject constructor() : ViewModel() {

    private val isLoadingState = MutableStateFlow<IsLoadingState>(IsLoadingState.NotLoading)
    private val eventFlow =
        MutableStateFlow<DeleteAllAttachmentsEvent>(DeleteAllAttachmentsEvent.Idle)

    val state = combine(
        isLoadingState,
        eventFlow
    ) { isLoadingState, event ->
        DeleteAllAttachmentsState(
            isDeleting = isLoadingState is IsLoadingState.Loading,
            event = event
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DeleteAllAttachmentsState.Idle
        )

    fun deleteAllAttachments() {
        isLoadingState.update { IsLoadingState.Loading }
        // delete
        isLoadingState.update { IsLoadingState.NotLoading }
        eventFlow.update { DeleteAllAttachmentsEvent.Close }
    }

    fun onConsumeEvent(event: DeleteAllAttachmentsEvent) {
        eventFlow.compareAndSet(event, DeleteAllAttachmentsEvent.Idle)
    }
}

data class DeleteAllAttachmentsState(
    val isDeleting: Boolean,
    val event: DeleteAllAttachmentsEvent
) {
    companion object {
        val Idle = DeleteAllAttachmentsState(
            isDeleting = false,
            event = DeleteAllAttachmentsEvent.Idle
        )
    }
}

sealed interface DeleteAllAttachmentsEvent {
    data object Idle : DeleteAllAttachmentsEvent
    data object Close : DeleteAllAttachmentsEvent
}
