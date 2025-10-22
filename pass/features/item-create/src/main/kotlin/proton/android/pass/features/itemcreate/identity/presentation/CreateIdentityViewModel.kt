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

package proton.android.pass.features.itemcreate.identity.presentation

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.features.itemcreate.ItemCreate
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.OptionShareIdSaver
import proton.android.pass.features.itemcreate.common.ShareUiState
import proton.android.pass.features.itemcreate.common.canDisplayWarningMessageForCreationFlow
import proton.android.pass.features.itemcreate.common.getShareUiStateFlow
import proton.android.pass.features.itemcreate.identity.presentation.IdentitySnackbarMessage.ItemCreated
import proton.android.pass.features.itemcreate.identity.presentation.IdentitySnackbarMessage.ItemCreationError
import proton.android.pass.inappreview.api.InAppReviewTriggerMetrics
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class CreateIdentityViewModel @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val createItem: CreateItem,
    private val identityActionsProvider: IdentityActionsProvider,
    private val telemetryManager: TelemetryManager,
    private val inAppReviewTriggerMetrics: InAppReviewTriggerMetrics,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val getItemById: GetItemById,
    observeVaults: ObserveVaultsWithItemCount,
    observeDefaultVault: ObserveDefaultVault,
    savedStateHandleProvider: SavedStateHandleProvider,
    observeShare: ObserveShare,
    private val settingsRepository: InternalSettingsRepository
) : ViewModel(), IdentityActionsProvider by identityActionsProvider {

    private val navShareId: Option<ShareId> =
        savedStateHandleProvider.get().get<String>(CommonOptionalNavArgId.ShareId.key)
            .toOption()
            .map(::ShareId)

    private val navItemId: Option<ItemId> =
        savedStateHandleProvider.get().get<String>(CommonOptionalNavArgId.ItemId.key)
            .toOption()
            .map(::ItemId)

    init {
        viewModelScope.launch {
            identityActionsProvider.observeActions(this)
        }
    }

    @OptIn(SavedStateHandleSaveableApi::class)
    private var selectedShareIdMutableState: Option<ShareId> by savedStateHandleProvider.get()
        .saveable(stateSaver = OptionShareIdSaver) { mutableStateOf(None) }
    private val selectedShareIdState: Flow<Option<ShareId>> =
        snapshotFlow { selectedShareIdMutableState }
            .filterNotNull()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = None
            )

    private val canDisplayWarningVaultSharedDialogFlow =
        canDisplayWarningMessageForCreationFlow(
            selectedShareIdMutableState = selectedShareIdMutableState,
            observeShare = observeShare,
            navShareId = navShareId,
            settingsRepository = settingsRepository
        )

    private val shareUiState: StateFlow<ShareUiState> = getShareUiStateFlow(
        navShareIdState = flowOf(navShareId),
        selectedShareIdState = selectedShareIdState,
        observeAllVaultsFlow = observeVaults(includeHidden = true).asLoadingResult(),
        observeDefaultVaultFlow = observeDefaultVault().asLoadingResult(),
        viewModelScope = viewModelScope,
        tag = TAG
    )

    val state: StateFlow<IdentityUiState> = combine(
        shareUiState,
        identityActionsProvider.observeSharedState(),
        canDisplayWarningVaultSharedDialogFlow
    ) { shareUiState, sharedState, canDisplayWarningVaultSharedDialog ->
        when (shareUiState) {
            is ShareUiState.Error -> IdentityUiState.Error
            is ShareUiState.Loading -> IdentityUiState.Loading
            is ShareUiState.Success -> IdentityUiState.CreateIdentity(
                shareUiState = shareUiState,
                sharedState = sharedState,
                isCloned = navItemId.isNotEmpty(),
                canDisplayVaultSharedWarningDialog = canDisplayWarningVaultSharedDialog
            )

            ShareUiState.NotInitialised -> IdentityUiState.NotInitialised
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = IdentityUiState.NotInitialised
    )

    fun onVaultSelect(shareId: ShareId) {
        selectedShareIdMutableState = Some(shareId)
    }

    suspend fun duplicateContents(context: Context) {
        val shareId = navShareId.value() ?: return
        val itemId = navItemId.value() ?: return
        val item = getItemById(shareId = shareId, itemId = itemId)
        val encryptedTitle = encryptionContextProvider.withEncryptionContextSuspendable {
            val decryptedTitle = context.getString(R.string.title_duplicate, decrypt(item.title))
            return@withEncryptionContextSuspendable encrypt(decryptedTitle)
        }
        identityActionsProvider.onItemReceivedState(item.copy(title = encryptedTitle))
    }

    internal fun doNotDisplayWarningDialog() {
        settingsRepository.setHasShownItemInSharedVaultWarning(true)
    }

    fun onSubmit(shareId: ShareId) = viewModelScope.launch {
        if (!identityActionsProvider.isFormStateValid()) return@launch
        identityActionsProvider.updateLoadingState(IsLoadingState.Loading)
        runCatching {
            createItem(
                shareId = shareId,
                itemContents = identityActionsProvider.getFormState().toItemContents()
            )
        }
            .onFailure {
                PassLogger.w(TAG, "Could not create item")
                PassLogger.w(TAG, it)
                snackbarDispatcher(ItemCreationError)
            }
            .onSuccess { item ->
                snackbarDispatcher(ItemCreated)
                inAppReviewTriggerMetrics.incrementItemCreatedCount()
                identityActionsProvider.onItemSavedState(item)
                telemetryManager.sendEvent(ItemCreate(EventItemType.Identity))
            }
        identityActionsProvider.updateLoadingState(IsLoadingState.NotLoading)
    }

    fun onRetryUploadDraftAttachment(metadata: FileMetadata) {
        viewModelScope.launch { identityActionsProvider.retryUploadDraftAttachment(metadata) }
    }

    fun dismissFileAttachmentsOnboarding() {
        viewModelScope.launch { identityActionsProvider.dismissFileAttachmentsOnboardingBanner() }
    }

    fun onPasteTotp() {
        viewModelScope.launch { identityActionsProvider.pasteTotp() }
    }

    companion object {
        private const val TAG = "CreateIdentityViewModel"
    }
}

