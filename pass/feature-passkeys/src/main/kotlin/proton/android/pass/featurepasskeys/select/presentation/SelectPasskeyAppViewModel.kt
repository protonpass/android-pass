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

package proton.android.pass.featurepasskeys.select.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.passkeys.GetPasskeyById
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.Passkey
import proton.android.pass.log.api.PassLogger
import proton.android.pass.passkeys.api.AuthenticateWithPasskey
import javax.inject.Inject

@Immutable
sealed interface SelectPasskeyAppEvent {
    @Immutable
    object Idle : SelectPasskeyAppEvent

    @Immutable
    object Cancel : SelectPasskeyAppEvent

    @Immutable
    data class SelectPasskeyFromItem(
        val item: ItemUiModel,
        val isLoadingState: IsLoadingState
    ) : SelectPasskeyAppEvent

    @Immutable
    @JvmInline
    value class SendResponse(val response: String) : SelectPasskeyAppEvent
}

@HiltViewModel
class SelectPasskeyAppViewModel @Inject constructor(
    private val authenticateWithPasskey: AuthenticateWithPasskey,
    private val getPasskeyById: GetPasskeyById,
) : ViewModel() {

    private val eventFlow: MutableStateFlow<SelectPasskeyAppEvent> =
        MutableStateFlow(SelectPasskeyAppEvent.Idle)

    val state: StateFlow<SelectPasskeyAppEvent> = eventFlow

    fun setInitialData(data: SelectPasskeyRequestData) = viewModelScope.launch {
        when (data) {
            is SelectPasskeyRequestData.SelectPasskey -> {}
            is SelectPasskeyRequestData.UsePasskey -> {
                val passkey = getPasskeyById(data.shareId, data.itemId, data.passkeyId)
                when (passkey) {
                    None -> {
                        PassLogger.w(TAG, "Passkey not found")
                        eventFlow.update { SelectPasskeyAppEvent.Cancel }
                        return@launch
                    }

                    is Some -> {
                        onPasskeySelected(
                            origin = data.domain,
                            passkey = passkey.value,
                            request = data.request
                        )
                    }
                }
            }
        }
    }

    fun onItemSelected(item: ItemUiModel, origin: String, request: String) = viewModelScope.launch {
        val itemContents = item.contents as? ItemContents.Login ?: run {
            PassLogger.w(TAG, "Received ItemContents that are not ItemContents.Login")
            eventFlow.update { SelectPasskeyAppEvent.Cancel }
            return@launch
        }

        when {
            // Only 1 passkey. Use that one
            itemContents.hasSinglePasskey -> {
                onPasskeySelected(
                    passkey = itemContents.passkeys.first(),
                    request = request,
                    origin = origin
                )
            }

            // Many passkeys. Choose which one
            itemContents.hasPasskeys -> {
                eventFlow.update {
                    SelectPasskeyAppEvent.SelectPasskeyFromItem(
                        item = item,
                        isLoadingState = IsLoadingState.NotLoading
                    )
                }
            }

            // No passkeys
            else -> {
                PassLogger.w(TAG, "Received ItemContents with no passkeys")
                eventFlow.update { SelectPasskeyAppEvent.Cancel }
            }

        }
    }

    fun onPasskeySelected(
        origin: String,
        passkey: Passkey,
        request: String
    ) = viewModelScope.launch {
        runCatching {
            authenticateWithPasskey(
                origin = origin,
                passkey = passkey,
                request = request
            )
        }.onSuccess { response ->
            PassLogger.i(TAG, "Successfully authenticated with passkey")
            eventFlow.update { SelectPasskeyAppEvent.SendResponse(response.response) }
        }.onFailure {
            PassLogger.w(TAG, "Error authenticating with passkey")
            PassLogger.w(TAG, it)
            eventFlow.update { SelectPasskeyAppEvent.Cancel }
        }
    }

    fun clearEvent() = viewModelScope.launch {
        eventFlow.update { SelectPasskeyAppEvent.Idle }
    }


    companion object {
        private const val TAG = "SelectPasskeyAppViewModel"
    }
}
