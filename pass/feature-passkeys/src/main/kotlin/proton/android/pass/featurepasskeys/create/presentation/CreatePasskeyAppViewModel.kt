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

package proton.android.pass.featurepasskeys.create.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.passkeys.StorePasskey
import proton.android.pass.passkeys.api.GeneratePasskey
import javax.inject.Inject

@Immutable
sealed interface CreatePasskeyAppEvent {
    @Immutable
    object Idle : CreatePasskeyAppEvent

    @Immutable
    data class AskForConfirmation(
        val item: ItemUiModel,
        val isLoadingState: IsLoadingState
    ) : CreatePasskeyAppEvent

    @Immutable
    @JvmInline
    value class SendResponse(val response: String) : CreatePasskeyAppEvent
}

@HiltViewModel
class CreatePasskeyAppViewModel @Inject constructor(
    private val generatePasskey: GeneratePasskey,
    private val storePasskey: StorePasskey
) : ViewModel() {

    private val eventFlow: MutableStateFlow<CreatePasskeyAppEvent> =
        MutableStateFlow(CreatePasskeyAppEvent.Idle)

    val state: StateFlow<CreatePasskeyAppEvent> = eventFlow

    fun onItemSelected(item: ItemUiModel) = viewModelScope.launch {
        eventFlow.update {
            CreatePasskeyAppEvent.AskForConfirmation(
                item = item,
                isLoadingState = IsLoadingState.NotLoading
            )
        }
    }

    fun onConfirmed(item: ItemUiModel, request: CreatePasskeyRequest) = viewModelScope.launch {
        eventFlow.update {
            CreatePasskeyAppEvent.AskForConfirmation(
                item = item,
                isLoadingState = IsLoadingState.Loading
            )
        }

        val passkey = generatePasskey(
            url = request.callingAppInfo.origin ?: "",
            request = request.callingRequest.requestJson
        )

        storePasskey(
            shareId = item.shareId,
            itemId = item.id,
            passkey = passkey.passkey
        )

        eventFlow.emit(CreatePasskeyAppEvent.SendResponse(passkey.response))
    }

    fun clearEvent() = viewModelScope.launch {
        eventFlow.emit(CreatePasskeyAppEvent.Idle)
    }

}
