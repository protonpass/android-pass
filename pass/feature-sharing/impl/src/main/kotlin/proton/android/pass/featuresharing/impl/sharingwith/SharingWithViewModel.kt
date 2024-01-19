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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.commonrust.api.EmailValidator
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.GetInviteUserMode
import proton.android.pass.data.api.usecases.InviteUserMode
import proton.android.pass.data.api.usecases.ObserveInviteRecommendations
import proton.android.pass.data.api.usecases.ObserveVaultById
import proton.android.pass.domain.ShareId
import proton.android.pass.featuresharing.impl.SharingWithUserModeType
import proton.android.pass.featuresharing.impl.ShowEditVaultArgId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import javax.inject.Inject

@HiltViewModel
class SharingWithViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val getInviteUserMode: GetInviteUserMode,
    private val emailValidator: EmailValidator,
    observeVaultById: ObserveVaultById,
    observeInviteRecommendations: ObserveInviteRecommendations,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val shareId: ShareId = ShareId(
        id = savedStateHandleProvider.get().require(CommonNavArgId.ShareId.key)
    )
    private val showEditVault: Boolean = savedStateHandleProvider.get()
        .require(ShowEditVaultArgId.key)

    private val isEmailNotValidState: MutableStateFlow<EmailNotValidReason?> =
        MutableStateFlow(null)
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val emailState: MutableStateFlow<String> =
        MutableStateFlow("")
    private val eventState: MutableStateFlow<SharingWithEvents> =
        MutableStateFlow(SharingWithEvents.Unknown)

    @OptIn(SavedStateHandleSaveableApi::class)
    private var checkedEmails: Set<String> by savedStateHandleProvider.get()
        .saveable { mutableStateOf(emptySet<String>()) }

    private val checkedEmailFlow = MutableStateFlow(checkedEmails)

    private val suggestionsUIStateFlow =
        combine(
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


    val state: StateFlow<SharingWithUIState> = combineN(
        emailState,
        isEmailNotValidState,
        observeVaultById(shareId = shareId),
        isLoadingState,
        eventState,
        suggestionsUIStateFlow
    ) { email, isEmailNotValid, vault, isLoading, event, suggestionsUIState ->
        val vaultValue = vault.value()
        SharingWithUIState(
            email = email,
            vault = vaultValue,
            emailNotValidReason = isEmailNotValid,
            isLoading = isLoading.value() || vaultValue == null,
            event = event,
            showEditVault = showEditVault,
            suggestionsUIState = suggestionsUIState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SharingWithUIState()
    )

    fun onEmailChange(value: String) {
        val sanitised = value.replace(" ", "").replace("\n", "")
        emailState.update { sanitised }
        isEmailNotValidState.update { null }
    }

    fun onEmailSubmit() = viewModelScope.launch {
        isLoadingState.update { IsLoadingState.Loading }
        val email = emailState.value
        val userId = accountManager.getPrimaryUserId().firstOrNull()
        userId ?: run {
            PassLogger.i(TAG, "User id not found")
            isEmailNotValidState.update { EmailNotValidReason.UserIdNotFound }
            isLoadingState.update { IsLoadingState.NotLoading }
            return@launch
        }

        if (email.isBlank() || !emailValidator.isValid(email)) {
            PassLogger.i(TAG, "Email not valid")
            isEmailNotValidState.update { EmailNotValidReason.NotValid }
            isLoadingState.update { IsLoadingState.NotLoading }
            return@launch
        }

        getInviteUserMode(userId, email)
            .onSuccess { userMode ->
                PassLogger.d(TAG, "Invite user mode: $userMode")
                eventState.update {
                    SharingWithEvents.NavigateToPermissions(
                        shareId = shareId,
                        email = email,
                        userMode = userMode.toUserModeType()
                    )
                }
            }
            .onFailure {
                PassLogger.w(TAG, "Error getting invite user mode")
                PassLogger.w(TAG, it)
                isEmailNotValidState.update { EmailNotValidReason.CannotGetEmailInfo }
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

    companion object {
        private const val TAG = "SharingWithViewModel"
    }
}

enum class EmailNotValidReason {
    NotValid,
    CannotGetEmailInfo,
    UserIdNotFound
}
