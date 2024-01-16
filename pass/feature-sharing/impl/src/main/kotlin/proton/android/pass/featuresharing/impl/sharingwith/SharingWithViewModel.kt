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

package proton.android.pass.featuresharing.impl.sharingwith

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.commonrust.api.EmailValidator
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.InviteUserMode
import proton.android.pass.data.api.usecases.ObserveInviteRecommendations
import proton.android.pass.data.api.repositories.BulkInviteRepository
import proton.android.pass.data.api.usecases.ObserveVaultById
import proton.android.pass.domain.ShareId
import proton.android.pass.featuresharing.impl.SharingWithUserModeType
import proton.android.pass.featuresharing.impl.ShowEditVaultArgId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import javax.inject.Inject

@HiltViewModel
class SharingWithViewModel @Inject constructor(
    private val emailValidator: EmailValidator,
    private val bulkInviteRepository: BulkInviteRepository,
    observeVaultById: ObserveVaultById,
    observeInviteRecommendations: ObserveInviteRecommendations,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val shareId: ShareId = ShareId(
        id = savedStateHandleProvider.get().require(CommonNavArgId.ShareId.key)
    )
    private val showEditVault: Boolean = savedStateHandleProvider.get()
        .require(ShowEditVaultArgId.key)

    private val showEmailNotValidFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val eventState: MutableStateFlow<SharingWithEvents> =
        MutableStateFlow(SharingWithEvents.Unknown)
    private val enteredEmailsState: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    private val selectedEmailIndexFlow: MutableStateFlow<Option<Int>> = MutableStateFlow(None)

    @OptIn(SavedStateHandleSaveableApi::class)
    private var editingEmailState by savedStateHandleProvider.get().saveable {
        mutableStateOf("")
    }

    @OptIn(SavedStateHandleSaveableApi::class)
    private var checkedEmails: Set<String> by savedStateHandleProvider.get()
        .saveable { mutableStateOf(emptySet<String>()) }

    private val checkedEmailFlow = MutableStateFlow(checkedEmails)

    private val suggestionsUIStateFlow = combine(
        observeInviteRecommendations(shareId = shareId).asLoadingResult(),
        checkedEmailFlow
    ) { result, checkedEmails ->
        when (result) {
            is LoadingResult.Error -> SuggestionsUIState.Initial
            LoadingResult.Loading -> SuggestionsUIState.Loading
            is LoadingResult.Success -> SuggestionsUIState.Content(
                groupDisplayName = result.data.groupDisplayName,
                recentEmails = result.data.recommendedEmails.map { email ->
                    email to checkedEmails.contains(email)
                }.toPersistentList(),
                planEmails = result.data.planRecommendedEmails.map { email ->
                    email to checkedEmails.contains(email)
                }.toPersistentList()
            )
        }
    }

    val editingEmail: String get() = editingEmailState

    val state: StateFlow<SharingWithUIState> = combineN(
        enteredEmailsState,
        showEmailNotValidFlow,
        observeVaultById(shareId = shareId),
        isLoadingState,
        eventState,
        suggestionsUIStateFlow,
        selectedEmailIndexFlow
    ) { emails, isEmailNotValid, vault, isLoading, event, suggestionsUiState, selectedEmailIndex ->
        val vaultValue = vault.value()
        SharingWithUIState(
            enteredEmails = emails.toPersistentList(),
            selectedEmailIndex = selectedEmailIndex,
            vault = vaultValue,
            showEmailNotValidError = isEmailNotValid,
            isLoading = isLoading.value() || vaultValue == null,
            event = event,
            showEditVault = showEditVault,
            suggestionsUIState = suggestionsUiState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SharingWithUIState()
    )

    fun onEmailChange(value: String) {
        val sanitised = value.replace(" ", "").replace("\n", "")
        editingEmailState = sanitised
        showEmailNotValidFlow.update { false }
        selectedEmailIndexFlow.update { None }
    }

    fun onEmailSubmit() = viewModelScope.launch {
        if (checkValidEmail()) {
            enteredEmailsState.update {
                if (!it.contains(editingEmailState)) {
                    it + editingEmailState
                } else {
                    it
                }
            }
            editingEmailState = ""
        }
    }

    fun onEmailClick(index: Int) = viewModelScope.launch {
        if (selectedEmailIndexFlow.value.value() == index) {
            enteredEmailsState.update {
                it.filterIndexed { idx, _ -> idx != index }
            }
            selectedEmailIndexFlow.update { None }
        } else {
            selectedEmailIndexFlow.update { index.some() }
        }
    }

    fun onContinueClick() = viewModelScope.launch {
        if (editingEmailState.isNotBlank()) {
            if (checkValidEmail()) {
                enteredEmailsState.update { it + editingEmailState }
                editingEmailState = ""
            } else {
                return@launch
            }
        }

        isLoadingState.update { IsLoadingState.Loading }

        val emails = enteredEmailsState.value
        bulkInviteRepository.storeAddresses(emails)
        eventState.update {
            SharingWithEvents.NavigateToPermissions(shareId = shareId)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun clearEvent() {
        eventState.update { SharingWithEvents.Unknown }
    }

    fun onItemToggle(email: String, checked: Boolean) {
        checkedEmails = if (!checked) {
            checkedEmails + email
        } else {
            checkedEmails - email
        }
        checkedEmailFlow.update { checkedEmails }
    }

    private fun InviteUserMode.toUserModeType(): SharingWithUserModeType = when (this) {
        InviteUserMode.NewUser -> SharingWithUserModeType.NewUser
        InviteUserMode.ExistingUser -> SharingWithUserModeType.ExistingUser
    }

    private fun checkValidEmail()
        : Boolean {
        if (editingEmailState.isBlank() || !emailValidator.isValid(editingEmailState)) {
            PassLogger.i(TAG, "Email not valid")
            showEmailNotValidFlow.update { true }
            return false
        }
        return true
    }

    companion object {
        private const val TAG = "SharingWithViewModel"
    }
}

