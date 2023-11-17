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

package proton.android.pass.featurevault.impl.bottomsheet

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.CannotCreateMoreVaultsError
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.data.api.usecases.DeleteVault
import proton.android.pass.data.api.usecases.MigrateItem
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.entity.NewVault
import proton.android.pass.featurevault.impl.VaultSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class CreateVaultViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val createVault: CreateVault,
    private val deleteVault: DeleteVault,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val savedStateHandleProvider: SavedStateHandleProvider,
    private val migrateItem: MigrateItem,
    observeUpgradeInfo: ObserveUpgradeInfo,
) : BaseVaultViewModel() {

    private val nextAction = getNextAction()

    val createState: StateFlow<CreateVaultUiState> = combine(
        state,
        observeUpgradeInfo().asLoadingResult()
    ) { baseState, upgrade ->
        val (isLoading, showUpgradeButton) = when (upgrade) {
            is LoadingResult.Success -> baseState.isLoading to upgrade.data.hasReachedVaultLimit()
            LoadingResult.Loading -> IsLoadingState.Loading to false
            is LoadingResult.Error -> {
                PassLogger.w(TAG, upgrade.exception, "Get upgrade info failed")
                baseState.isLoading to false
            }
        }

        CreateVaultUiState(
            base = baseState.copy(isLoading = isLoading),
            displayNeedUpgrade = showUpgradeButton
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = CreateVaultUiState.Initial
    )

    fun onCreateClick() = viewModelScope.launch {
        if (formFlow.value.name.isBlank()) return@launch

        isLoadingFlow.update { IsLoadingState.Loading }

        val form = formFlow.value
        val body = encryptionContextProvider.withEncryptionContext {
            NewVault(
                name = encrypt(form.name.trim()),
                description = encrypt(""),
                icon = form.icon,
                color = form.color
            )
        }

        PassLogger.d(TAG, "Sending Create Vault request")
        runCatching { createVault(vault = body) }
            .onSuccess {
                onVaultCreated(it)
            }
            .onFailure {
                val message = if (it is CannotCreateMoreVaultsError) {
                    VaultSnackbarMessage.CannotCreateMoreVaultsError
                } else {
                    PassLogger.w(TAG, it, "Create vault failed")
                    VaultSnackbarMessage.CreateVaultError
                }
                snackbarDispatcher(message)
            }

        isLoadingFlow.update { IsLoadingState.NotLoading }
    }

    private suspend fun onVaultCreated(newVault: Share) {
        PassLogger.d(TAG, "Vault created successfully")

        when (val action = nextAction) {
            CreateVaultNextAction.Done -> {
                snackbarDispatcher(VaultSnackbarMessage.CreateVaultSuccess)
                eventFlow.update { IsVaultCreatedEvent.Created }
            }

            is CreateVaultNextAction.ShareVault -> {
                PassLogger.d(TAG, "Migrating item")
                runCatching {
                    migrateItem(
                        sourceShare = action.shareId,
                        itemId = action.itemId,
                        destinationShare = newVault.id
                    )
                }.onSuccess {
                    PassLogger.d(TAG, "Item migrated successfully")
                    snackbarDispatcher(VaultSnackbarMessage.CreateVaultSuccess)
                    eventFlow.update { IsVaultCreatedEvent.CreatedAndMoveToShare(newVault.id) }
                }.onFailure { migrateItemError ->
                    PassLogger.w(TAG, migrateItemError, "Migrate item failed. Deleting vault")
                    runCatching {
                        deleteVault(newVault.id)
                    }.onSuccess {
                        PassLogger.d(TAG, "Vault deleted successfully")
                        snackbarDispatcher(VaultSnackbarMessage.CreateVaultError)
                    }.onFailure { deleteVaultError ->
                        PassLogger.w(TAG, deleteVaultError, "Could not delete vault")
                        snackbarDispatcher(VaultSnackbarMessage.CreateVaultError)
                    }
                }
            }
        }
    }

    private fun getNextAction(): CreateVaultNextAction {
        val savedState = savedStateHandleProvider.get()
        val nextAction = savedState.require<String>(CreateVaultNextActionNavArgId.key)
        return when (nextAction) {
            CreateVaultNextAction.NEXT_ACTION_DONE -> CreateVaultNextAction.Done
            CreateVaultNextAction.NEXT_ACTION_SHARE -> {
                val shareId = ShareId(savedState.require(CommonOptionalNavArgId.ShareId.key))
                val itemId = ItemId(savedState.require(CommonOptionalNavArgId.ItemId.key))
                CreateVaultNextAction.ShareVault(shareId, itemId)
            }

            else -> throw IllegalArgumentException("Unknown next action $nextAction")
        }
    }

    companion object {
        private const val TAG = "CreateVaultViewModel"
    }
}
