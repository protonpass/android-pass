/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.username.bottomsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.log.api.PassLogger
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.commonrust.api.usernames.UsernameConfig
import proton.android.pass.commonrust.api.usernames.UsernameGenerator
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.repositories.DRAFT_USERNAME_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.usecases.usernames.ObserveUsernameConfig
import proton.android.pass.data.api.usecases.usernames.UpdateUsernameConfig
import proton.android.pass.features.username.GenerateUsernameBottomsheetMode
import proton.android.pass.features.username.GenerateUsernameBottomsheetModeValue
import proton.android.pass.features.username.GenerateUsernameSnackbarMessage
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class GenerateUsernameViewModel @Inject constructor(
    stateHandleProvider: SavedStateHandleProvider,
    observeUsernameConfig: ObserveUsernameConfig,
    usernameGenerator: UsernameGenerator,
    private val updateUsernameConfig: UpdateUsernameConfig,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    private val draftRepository: DraftRepository
) : ViewModel() {

    private val mode = stateHandleProvider.get()
        .require<String>(GenerateUsernameBottomsheetMode.key)
        .let { value ->
            when (GenerateUsernameBottomsheetModeValue.valueOf(value)) {
                GenerateUsernameBottomsheetModeValue.CancelConfirm -> GenerateUsernameMode.CancelConfirm
                GenerateUsernameBottomsheetModeValue.CopyAndClose -> GenerateUsernameMode.CopyAndClose
            }
        }

    private val configFlow = observeUsernameConfig()
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            replay = 1
        )

    private val regenerateFlow = MutableStateFlow(false)

    private val usernameFlow = combine(
        configFlow,
        regenerateFlow
            .onStart { emit(true) }
            .onEach { regenerateFlow.update { false } }
            .filter { it }
    ) { config, _ ->
        runCatching { usernameGenerator.generateUsername(config) }
            .getOrElse { error ->
                PassLogger.w(TAG, "Failed to generate username")
                PassLogger.w(TAG, error)
                snackbarDispatcher(GenerateUsernameSnackbarMessage.Error)
                ""
            }
    }.catch { error ->
        PassLogger.w(TAG, "Username flow failure")
        PassLogger.w(TAG, error)
        snackbarDispatcher(GenerateUsernameSnackbarMessage.Error)
        emit("")
    }.shareIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        replay = 1
    )

    private val eventFlow = MutableStateFlow<GenerateUsernameEvent>(GenerateUsernameEvent.Idle)

    val stateFlow: StateFlow<GenerateUsernameUiState> = combine(
        usernameFlow,
        configFlow,
        eventFlow
    ) { username, config, event ->
        GenerateUsernameUiState(
            username = username,
            config = config,
            mode = mode,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GenerateUsernameUiState.initial(mode)
    )

    fun onConsumeEvent(event: GenerateUsernameEvent) {
        eventFlow.compareAndSet(event, GenerateUsernameEvent.Idle)
    }

    fun onChangeConfig(newConfig: UsernameConfig) {
        viewModelScope.launch { updateUsernameConfig(newConfig) }
    }

    fun onRegenerate() {
        regenerateFlow.update { true }
    }

    fun onConfirm() {
        draftRepository.save(DRAFT_USERNAME_KEY, stateFlow.value.username)
        eventFlow.update { GenerateUsernameEvent.OnUsernameConfirmed }
    }

    fun onCopy() {
        clipboardManager.copyToClipboard(stateFlow.value.username, isSecure = false)
        viewModelScope.launch {
            snackbarDispatcher(GenerateUsernameSnackbarMessage.CopiedToClipboard)
            eventFlow.update { GenerateUsernameEvent.OnUsernameCopied }
        }
    }

    private companion object {
        private const val TAG = "GenerateUsernameViewModel"
    }
}
