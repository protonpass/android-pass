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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.passkeys.StorePasskey
import proton.android.pass.featureitemcreate.impl.login.InitialCreateLoginUiState
import proton.android.pass.featurepasskeys.create.CreatePasskeySnackbarMessage
import proton.android.pass.featureselectitem.navigation.SelectItemState
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.passkeys.api.GeneratePasskey
import javax.inject.Inject

@Immutable
sealed interface CreatePasskeyAppEvent {
    @Immutable
    data object Idle : CreatePasskeyAppEvent

    @Immutable
    data class AskForConfirmation(
        val item: ItemUiModel,
        val isLoadingState: IsLoadingState
    ) : CreatePasskeyAppEvent

    @Immutable
    @JvmInline
    value class SendResponse(val response: String) : CreatePasskeyAppEvent
}

@Immutable
sealed interface CreatePasskeyNavState {
    @Immutable
    data object Loading : CreatePasskeyNavState

    @Immutable
    data class Ready(
        val selectItemState: SelectItemState,
        val createLoginUiState: InitialCreateLoginUiState
    ) : CreatePasskeyNavState
}

@Immutable
data class CreatePasskeyAppUiState(
    val event: CreatePasskeyAppEvent,
    val navState: CreatePasskeyNavState
)

@HiltViewModel
class CreatePasskeyAppViewModel @Inject constructor(
    private val generatePasskey: GeneratePasskey,
    private val storePasskey: StorePasskey,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val eventFlow: MutableStateFlow<CreatePasskeyAppEvent> =
        MutableStateFlow(CreatePasskeyAppEvent.Idle)

    private val requestStateFlow: MutableStateFlow<Option<AppStateRequest>> =
        MutableStateFlow(None)

    private val selectItemStateFlow: Flow<Option<SelectItemState>> = requestStateFlow
        .mapLatest { value ->
            value.map { stateRequest ->
                SelectItemState.Passkey.Register(
                    title = stateRequest.appState.data.domain,
                    suggestionsUrl = stateRequest.request.callingRequest.origin.toOption()
                )
            }
        }

    private val createLoginUiStateFlow: Flow<Option<InitialCreateLoginUiState>> = requestStateFlow
        .mapLatest { value ->
            value.map { stateRequest ->
                val passkeyOrigin = stateRequest.request.callingRequest.origin
                    ?: stateRequest.appState.data.origin

                InitialCreateLoginUiState(
                    title = stateRequest.appState.data.rpName,
                    username = stateRequest.appState.data.username,
                    url = stateRequest.request.callingRequest.origin,
                    passkeyOrigin = passkeyOrigin,
                    passkeyRequest = stateRequest.request.callingRequest.requestJson,
                    passkeyDomain = stateRequest.appState.data.domain
                )
            }
        }

    val state: StateFlow<CreatePasskeyAppUiState> = combine(
        eventFlow,
        selectItemStateFlow,
        createLoginUiStateFlow
    ) { event, selectItem, createLogin ->
        val navState = when (selectItem) {
            None -> CreatePasskeyNavState.Loading
            is Some -> when (createLogin) {
                None -> CreatePasskeyNavState.Loading
                is Some -> CreatePasskeyNavState.Ready(
                    selectItemState = selectItem.value,
                    createLoginUiState = createLogin.value
                )
            }
        }

        CreatePasskeyAppUiState(
            event = event,
            navState = navState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = CreatePasskeyAppUiState(
            event = CreatePasskeyAppEvent.Idle,
            navState = CreatePasskeyNavState.Loading
        )
    )

    fun setInitialData(request: CreatePasskeyRequest, appState: CreatePasskeyAppState.Ready) {
        requestStateFlow.update { AppStateRequest(request, appState).some() }
    }

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

        runCatching {
            val passkey = generatePasskey(
                url = request.callingAppInfo.origin ?: "",
                request = request.callingRequest.requestJson
            )
            storePasskey(
                shareId = item.shareId,
                itemId = item.id,
                passkey = passkey.passkey
            )
            passkey
        }.onSuccess { passkey ->
            eventFlow.emit(CreatePasskeyAppEvent.SendResponse(passkey.response))
        }.onFailure {
            PassLogger.w(TAG, "Error generating passkey")
            PassLogger.w(TAG, it)
            snackbarDispatcher(CreatePasskeySnackbarMessage.ErrorGeneratingPasskey)

            eventFlow.update { CreatePasskeyAppEvent.Idle }
        }
    }

    fun clearEvent() = viewModelScope.launch {
        eventFlow.emit(CreatePasskeyAppEvent.Idle)
    }

    private data class AppStateRequest(
        val request: CreatePasskeyRequest,
        val appState: CreatePasskeyAppState.Ready
    )

    companion object {
        private const val TAG = "CreatePasskeyAppViewModel"
    }

}
