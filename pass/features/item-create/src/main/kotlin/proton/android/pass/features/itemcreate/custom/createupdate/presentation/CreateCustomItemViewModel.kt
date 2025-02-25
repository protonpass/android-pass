/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.custom.createupdate.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
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
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.ItemCreate
import proton.android.pass.features.itemcreate.common.CustomItemFieldDraftRepository
import proton.android.pass.features.itemcreate.common.OptionShareIdSaver
import proton.android.pass.features.itemcreate.common.ShareUiState
import proton.android.pass.features.itemcreate.common.attachments.AttachmentsHandler
import proton.android.pass.features.itemcreate.common.getShareUiStateFlow
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.CreateSpecificIntent.OnVaultSelected
import proton.android.pass.features.itemcreate.custom.createupdate.presentation.CreateSpecificIntent.SubmitCreate
import proton.android.pass.inappreview.api.InAppReviewTriggerMetrics
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class CreateCustomItemViewModel @Inject constructor(
    private val createItem: CreateItem,
    private val telemetryManager: TelemetryManager,
    private val inAppReviewTriggerMetrics: InAppReviewTriggerMetrics,
    private val snackbarDispatcher: SnackbarDispatcher,
    observeVaults: ObserveVaultsWithItemCount,
    observeDefaultVault: ObserveDefaultVault,
    attachmentsHandler: AttachmentsHandler,
    draftRepository: DraftRepository,
    customItemFieldDraftRepository: CustomItemFieldDraftRepository,
    encryptionContextProvider: EncryptionContextProvider,
    savedStateHandleProvider: SavedStateHandleProvider
) : BaseCustomItemViewModel(
    attachmentsHandler = attachmentsHandler,
    draftRepository = draftRepository,
    encryptionContextProvider = encryptionContextProvider,
    customItemFieldDraftRepository = customItemFieldDraftRepository
) {

    private val navShareId: Option<ShareId> =
        savedStateHandleProvider.get().get<String>(CommonOptionalNavArgId.ShareId.key)
            .toOption()
            .map(::ShareId)

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

    private val shareUiState: StateFlow<ShareUiState> = getShareUiStateFlow(
        navShareIdState = flowOf(navShareId),
        selectedShareIdState = selectedShareIdState,
        observeAllVaultsFlow = observeVaults().asLoadingResult(),
        observeDefaultVaultFlow = observeDefaultVault().asLoadingResult(),
        viewModelScope = viewModelScope,
        tag = TAG
    )

    val state = combine(
        shareUiState,
        observeSharedState()
    ) { shareUiState, sharedState ->
        when (shareUiState) {
            is ShareUiState.Error -> CustomItemState.Error
            is ShareUiState.Loading -> CustomItemState.Loading
            is ShareUiState.Success ->
                CustomItemState.CreateCustomItemState(shareUiState, sharedState)

            ShareUiState.NotInitialised -> CustomItemState.NotInitialised
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CustomItemState.NotInitialised
        )

    fun processIntent(intent: BaseItemFormIntent) {
        when (intent) {
            is BaseCustomItemCommonIntent -> processCommonIntent(intent)
            is CreateSpecificIntent -> processSpecificIntent(intent)
            else -> throw IllegalArgumentException("Unknown intent: $intent")
        }
    }

    private fun processSpecificIntent(intent: CreateSpecificIntent) {
        when (intent) {
            is SubmitCreate -> onSubmitCreate(intent.shareId)
            is OnVaultSelected -> onVaultSelected(intent.shareId)
        }
    }

    private fun onSubmitCreate(shareId: ShareId) {
        viewModelScope.launch {
            if (!isFormStateValid()) return@launch
            updateLoadingState(IsLoadingState.Loading)
            runCatching {
                createItem(
                    shareId = shareId,
                    itemContents = getFormState().toItemContents()
                )
            }
                .onFailure {
                    PassLogger.w(TAG, "Could not create item")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(CustomItemSnackbarMessage.ItemCreationError)
                }
                .onSuccess { item ->
                    inAppReviewTriggerMetrics.incrementItemCreatedCount()
                    TODO()
                    // onItemSavedState(item)
                    telemetryManager.sendEvent(ItemCreate(EventItemType.Custom))
                    snackbarDispatcher(CustomItemSnackbarMessage.ItemCreated)
                }
            updateLoadingState(IsLoadingState.NotLoading)
        }
    }

    private fun onVaultSelected(shareId: ShareId) {
        selectedShareIdMutableState = Some(shareId)
    }

    companion object {
        private const val TAG = "CreateCustomItemViewModel"
    }
}

sealed interface CreateSpecificIntent : BaseItemFormIntent {
    @JvmInline
    value class SubmitCreate(val shareId: ShareId) : CreateSpecificIntent

    @JvmInline
    value class OnVaultSelected(val shareId: ShareId) : CreateSpecificIntent
}

