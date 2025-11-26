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

package proton.android.pass.features.sharing.sharingwith

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.util.kotlin.takeIfNotBlank
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.map
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonrust.api.EmailValidator
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.repositories.BulkInviteRepository
import proton.android.pass.data.api.repositories.GroupTarget
import proton.android.pass.data.api.repositories.InviteTarget
import proton.android.pass.data.api.repositories.UserTarget
import proton.android.pass.data.api.usecases.CanAddressesBeInvitedResult
import proton.android.pass.data.api.usecases.CheckCanAddressesBeInvited
import proton.android.pass.data.api.usecases.ObserveInviteRecommendations
import proton.android.pass.data.api.usecases.organization.ObserveOrganizationSettings
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.OrganizationSettings
import proton.android.pass.domain.OrganizationShareMode
import proton.android.pass.domain.RecommendedEmail
import proton.android.pass.domain.RecommendedGroup
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.features.sharing.ShowEditVaultArgId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import javax.inject.Inject

@HiltViewModel
class SharingWithViewModel @Inject constructor(
    private val emailValidator: EmailValidator,
    private val bulkInviteRepository: BulkInviteRepository,
    private val checkCanAddressesBeInvited: CheckCanAddressesBeInvited,
    observeShare: ObserveShare,
    observeInviteRecommendations: ObserveInviteRecommendations,
    observeOrganizationSettings: ObserveOrganizationSettings,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val itemIdOption: Option<ItemId> = savedStateHandleProvider.get()
        .get<String>(CommonOptionalNavArgId.ItemId.key)
        .toOption()
        .map(::ItemId)

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val showEditVault: Boolean = savedStateHandleProvider.get()
        .require(ShowEditVaultArgId.key)

    private val scrollToBottomFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val eventState: MutableStateFlow<SharingWithEvents> =
        MutableStateFlow(SharingWithEvents.Idle)

    private val enteredEmailsState: MutableStateFlow<List<EnteredEmailUiModel>> =
        MutableStateFlow(emptyList())

    private val focusedEmailIndexFlow: MutableStateFlow<Option<Int>> = MutableStateFlow(None)

    private val checkedEmailFlow = MutableStateFlow<Set<String>>(emptySet())
    private val checkedGroupIdsFlow = MutableStateFlow<Set<GroupId>>(emptySet())

    private val inviteEmailsState = combine(
        bulkInviteRepository.observeInvalidAddresses(),
        enteredEmailsState,
        focusedEmailIndexFlow
    ) { invalidAddresses, enteredEmails, focusedEmailIndex ->
        enteredEmails.mapIndexed { idx, enteredEmail ->
            if (invalidAddresses.contains(enteredEmail.email)) {
                enteredEmail.copy(isError = true, isFocused = idx == focusedEmailIndex.value())
            } else {
                enteredEmail.copy(isFocused = idx == focusedEmailIndex.value())
            }
        }
    }

    private val organizationSettingsFlow: Flow<LoadingResult<Option<OrganizationSettings>>> =
        observeOrganizationSettings().asLoadingResult().distinctUntilChanged()
    private val errorMessageFlow: MutableStateFlow<ErrorMessage> =
        MutableStateFlow(ErrorMessage.None)

    @OptIn(SavedStateHandleSaveableApi::class)
    private var editingEmailState by savedStateHandleProvider.get()
        .saveable { mutableStateOf("") }

    private val editingEmailStateFlow: MutableStateFlow<String> =
        MutableStateFlow(editingEmailState)

    @OptIn(FlowPreview::class)
    private val debouncedEditingEmailStateFlow = editingEmailStateFlow
        .debounce(DEBOUNCE_TIMEOUT)
        .onStart { emit("") }
        .distinctUntilChanged()

    private val recommendationsFlow = debouncedEditingEmailStateFlow
        .flatMapLatest { email ->
            observeInviteRecommendations(
                shareId = shareId,
                startsWith = email.takeIfNotBlank().toOption()
            ).asLoadingResult()
        }

    private val suggestionsUIStateFlow = combine(
        recommendationsFlow,
        checkedEmailFlow,
        checkedGroupIdsFlow
    ) { result, checkedEmails, checkedGroupIds ->
        when (result) {
            is LoadingResult.Error -> SuggestionsUIState.Initial
            LoadingResult.Loading -> SuggestionsUIState.Loading
            is LoadingResult.Success -> {
                val recommendations = result.data
                val recentEmails = recommendations.recommendedItems
                    .filterIsInstance<RecommendedEmail>()
                    .map { EmailUiModel(it.email, checkedEmails.contains(it.email)) }

                val recentGroups = recommendations.recommendedItems
                    .filterIsInstance<RecommendedGroup>()
                    .map { group ->
                        GroupSuggestionUiModel(
                            id = group.groupId,
                            email = group.email,
                            name = group.name,
                            memberCount = group.memberCount,
                            isSelected = group.groupId in checkedGroupIds
                        )
                    }

                val organizationEmails = recommendations.organizationItems
                    .filterIsInstance<RecommendedEmail>()
                    .map { EmailUiModel(it.email, checkedEmails.contains(it.email)) }

                val organizationGroups = recommendations.organizationItems
                    .filterIsInstance<RecommendedGroup>()
                    .map { group ->
                        GroupSuggestionUiModel(
                            id = group.groupId,
                            email = group.email,
                            name = group.name,
                            memberCount = group.memberCount,
                            isSelected = group.groupId in checkedGroupIds
                        )
                    }

                val recentSortedItems = (recentEmails + recentGroups)
                    .sortedBy { it.sortKey }
                    .toPersistentList()

                val organizationSortedItems = (organizationEmails + organizationGroups)
                    .sortedBy { it.sortKey }
                    .toPersistentList()

                SuggestionsUIState.Content(
                    groupDisplayName = recommendations.groupDisplayName,
                    recentSortedItems = recentSortedItems,
                    organizationSortedItems = organizationSortedItems
                )
            }
        }
    }

    private val continueEnabledFlow = combine(
        inviteEmailsState,
        checkedGroupIdsFlow,
        editingEmailStateFlow
    ) { inviteEmails, selectedGroups, editingEmail ->
        if (inviteEmails.any { it.isError }) {
            false
        } else {
            inviteEmails.isNotEmpty() || selectedGroups.isNotEmpty() || editingEmail.isNotBlank()
        }
    }

    internal val editingEmail: String
        get() = editingEmailState

    internal val stateFlow: StateFlow<SharingWithUIState> = combineN(
        inviteEmailsState,
        observeShare(shareId = shareId),
        isLoadingState,
        eventState,
        suggestionsUIStateFlow,
        scrollToBottomFlow,
        continueEnabledFlow,
        organizationSettingsFlow,
        errorMessageFlow
    ) { emails, share, isLoading, event, suggestionsUiState,
        scrollToBottom, continueEnabled, organizationSettingsResult, errorMessage ->

        val canOnlyPickFromSelection =
            organizationSettingsResult.map { organizationSettingsOption ->
                organizationSettingsOption.map {
                    when (it) {
                        OrganizationSettings.NotAnOrganization -> false
                        is OrganizationSettings.Organization -> when (it.shareMode) {
                            OrganizationShareMode.Unrestricted -> false
                            OrganizationShareMode.OrganizationOnly -> true
                        }
                    }
                }.value() ?: true
            }.getOrNull() ?: true

        SharingWithUIState(
            enteredEmails = emails.toPersistentList(),
            share = share,
            isLoading = isLoading.value(),
            event = event,
            showEditVault = showEditVault,
            suggestionsUIState = suggestionsUiState,
            scrollToBottom = scrollToBottom,
            isContinueEnabled = continueEnabled,
            canOnlyPickFromSelection = canOnlyPickFromSelection,
            errorMessage = errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SharingWithUIState()
    )

    internal fun onEmailChange(value: String) {
        val sanitised = value.replace(" ", "").replace("\n", "")
        editingEmailState = sanitised
        editingEmailStateFlow.update { sanitised }
        errorMessageFlow.update { ErrorMessage.None }
        focusedEmailIndexFlow.update { None }
    }

    internal fun onEmailSubmit() {
        if (checkValidEmail()) {
            enteredEmailsState.update {
                if (!it.contains(editingEmailState)) {
                    scrollToBottomFlow.update { true }
                    val newValue = it + EnteredEmailUiModel(editingEmailState)
                    editingEmailState = ""
                    editingEmailStateFlow.update { "" }
                    newValue
                } else {
                    errorMessageFlow.update { ErrorMessage.EmailAlreadyAdded }
                    it
                }
            }
        } else {
            errorMessageFlow.update { ErrorMessage.EmailNotValid }
        }
    }

    internal fun onChipEmailClick(index: Int) {
        if (focusedEmailIndexFlow.value.value() == index) {
            enteredEmailsState.update { currentList ->
                if (index !in currentList.indices) return@update currentList
                val email = currentList[index]
                checkedEmailFlow.update {
                    if (checkedEmailFlow.value.contains(email.email)) {
                        checkedEmailFlow.value - email.email
                    } else {
                        checkedEmailFlow.value + email.email
                    }
                }
                currentList.filterIndexed { idx, _ -> idx != index }
            }
            focusedEmailIndexFlow.update { None }
        } else {
            focusedEmailIndexFlow.update { index.some() }
        }
    }

    internal fun onContinueClick() {
        viewModelScope.launch {
            errorMessageFlow.update { ErrorMessage.None }

            // If the user is still editing an email, try to add it to the list before continuing
            if (editingEmailState.isNotBlank() && !addCurrentEmailToListIfPossible()) {
                return@launch
            }

            isLoadingState.update { IsLoadingState.Loading }

            val userEmails = enteredEmailsState.value.map { it.email }
            val selectedGroups = stateFlow.value.selectedGroups
            val groupEmails = selectedGroups.map { it.email }

            // Check to see if all addresses can be invited
            val canInviteResult = checkCanAddressesBeInvited(
                shareId = shareId,
                addresses = (userEmails + groupEmails).distinct()
            )

            handleCanInviteResult(canInviteResult, userEmails, selectedGroups)

            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    internal fun onConsumeEvent(events: SharingWithEvents) {
        eventState.compareAndSet(events, SharingWithEvents.Idle)
    }

    internal fun onItemToggle(email: String, checked: Boolean) {
        checkedEmailFlow.update { checkedEmails ->
            if (!checked) {
                enteredEmailsState.update { current ->
                    if (current.any { it.email == email }) {
                        current
                    } else {
                        scrollToBottomFlow.update { true }
                        current + EnteredEmailUiModel(email = email)
                    }
                }
                editingEmailState = ""
                editingEmailStateFlow.update { "" }
                checkedEmails + email
            } else {
                enteredEmailsState.update { current ->
                    if (current.any { it.email == email }) {
                        errorMessageFlow.update { ErrorMessage.None }
                        current.filterNot { it.email == email }
                    } else {
                        current
                    }
                }
                checkedEmails - email
            }
        }
    }

    internal fun onGroupToggle(groupId: GroupId, isSelected: Boolean) {
        val shouldSelect = !isSelected
        checkedGroupIdsFlow.update { current ->
            if (shouldSelect) {
                current + groupId
            } else {
                current - groupId
            }
        }
        if (shouldSelect) {
            scrollToBottomFlow.update { true }
        }
    }

    internal fun onScrolledToBottom() {
        scrollToBottomFlow.update { false }
    }

    private fun addCurrentEmailToListIfPossible(): Boolean {
        if (checkValidEmail()) {
            if (enteredEmailsState.value.contains(editingEmailState)) {
                errorMessageFlow.update { ErrorMessage.EmailAlreadyAdded }
                return false
            }

            enteredEmailsState.update { it + EnteredEmailUiModel(editingEmailState) }
            editingEmailState = ""
            editingEmailStateFlow.update { "" }
            return true
        } else {
            errorMessageFlow.update { ErrorMessage.EmailNotValid }
        }

        return false
    }

    private fun buildInviteTargets(
        userEmails: List<String>,
        selectedGroups: Set<GroupSuggestionUiModel>
    ): List<InviteTarget> {
        val userTargets = userEmails.map { UserTarget(email = it, shareRole = ShareRole.Read) }
        val groupTargets = selectedGroups.map {
            GroupTarget(
                groupId = it.id,
                name = it.name,
                memberCount = it.memberCount,
                email = it.email,
                shareRole = ShareRole.Read
            )
        }
        return userTargets + groupTargets
    }

    private fun handleCanInviteResult(
        canInviteResult: CanAddressesBeInvitedResult,
        userEmails: List<String>,
        selectedGroups: Set<GroupSuggestionUiModel>
    ) {
        when (canInviteResult) {
            // If all can be invited, proceed
            is CanAddressesBeInvitedResult.All -> {
                val inviteTargets = buildInviteTargets(userEmails, selectedGroups)
                bulkInviteRepository.storeInvites(inviteTargets)
                bulkInviteRepository.clearInvalidAddresses()
                eventState.update { SharingWithEvents.NavigateToPermissions(shareId, itemIdOption) }
            }

            // If none can be invited, show an error
            is CanAddressesBeInvitedResult.None -> {
                enteredEmailsState.update { currentEmails ->
                    currentEmails.map { it.copy(isError = true) }
                }
                errorMessageFlow.update {
                    when (canInviteResult.reason) {
                        CanAddressesBeInvitedResult.CannotInviteAddressReason.Unknown -> {
                            PassLogger.i(TAG, "Error checking if addresses can be invited")
                            ErrorMessage.NoAddressesCanBeInvited
                        }

                        CanAddressesBeInvitedResult.CannotInviteAddressReason.Empty -> {
                            PassLogger.i(TAG, "No addresses to invite")
                            ErrorMessage.NoAddressesCanBeInvited
                        }

                        CanAddressesBeInvitedResult.CannotInviteAddressReason.CannotInviteOutsideOrg -> {
                            PassLogger.i(TAG, "Cannot invite outside org")
                            ErrorMessage.CannotInviteOutsideOrg
                        }
                    }
                }
            }

            // If some can be invited, show an error and highlight the ones that can't be invited
            is CanAddressesBeInvitedResult.Some -> {
                val cannotBeInvited = canInviteResult.cannotBe
                enteredEmailsState.update { currentEmailStates ->
                    val newList = mutableListOf<EnteredEmailUiModel>()

                    for (emailState in currentEmailStates) {
                        if (emailState.email in cannotBeInvited) {
                            newList.add(emailState.copy(isError = true))
                        } else {
                            newList.add(emailState)
                        }
                    }

                    newList
                }

                errorMessageFlow.update { ErrorMessage.SomeAddressesCannotBeInvited }
            }
        }
    }

    private fun checkValidEmail(): Boolean {
        if (editingEmailState.isBlank() || !emailValidator.isValid(editingEmailState)) {
            PassLogger.i(TAG, "Email not valid")
            errorMessageFlow.update { ErrorMessage.EmailNotValid }
            return false
        }
        return true
    }

    private fun List<EnteredEmailUiModel>.contains(email: String) = any { it.email == email }

    private companion object {

        private const val DEBOUNCE_TIMEOUT = 300L

        private const val TAG = "SharingWithViewModel"

    }

}

