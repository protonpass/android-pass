/*
 * Copyright (c) 2025-2026 Proton AG
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

package proton.android.pass.features.itemcreate.common

import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.usecases.ObserveItemById
import proton.android.pass.data.api.usecases.defaultvault.SetDefaultVault
import proton.android.pass.data.api.usecases.shares.ObserveShare
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.InternalSettingsRepository

fun canDisplayWarningMessageForCreationFlow(
    selectedShareIdMutableState: Option<ShareId>?,
    observeShare: ObserveShare,
    navShareId: Option<ShareId>,
    settingsRepository: InternalSettingsRepository
) = snapshotFlow { selectedShareIdMutableState }
    .filterNotNull()
    .flatMapLatest { selectedShareIdMutableState ->
        when {
            selectedShareIdMutableState is Some -> {
                observeShare(shareId = selectedShareIdMutableState.value()!!)
            }

            navShareId.value() != null -> {
                observeShare(shareId = navShareId.value()!!)
            }

            else -> flowOf(null)
        }
    }
    .filterNotNull()
    .flatMapLatest { vault ->
        settingsRepository.hasShownItemInSharedVaultWarning().map {
            Pair(vault, it)
        }
    }
    .flatMapLatest {
        val vault = it.first
        val hasShownItemInSharedVaultWarning = it.second
        flowOf(vault.shared && !hasShownItemInSharedVaultWarning)
    }
    .catch { emit(false) }
    .onStart { emit(false) }


fun canDisplayVaultSharedWarningDialogFlow(
    settingsRepository: InternalSettingsRepository,
    shareId: ShareId,
    observeShare: ObserveShare,
    userId: UserId? = null
) = combine(
    settingsRepository.hasShownItemInSharedVaultWarning(),
    observeShare(shareId = shareId, userId = userId)
) { hasShownItemInSharedVaultWarning, share ->
    !hasShownItemInSharedVaultWarning && share.shared
}.catch { emit(false) }
    .onStart { emit(false) }

@Suppress("UnnecessaryParentheses")
fun canDisplaySharedItemWarningDialogFlow(
    settingsRepository: InternalSettingsRepository,
    shareId: ShareId,
    itemId: ItemId,
    observeItemById: ObserveItemById,
    userId: UserId? = null
) = combine(
    settingsRepository.hasShownItemInSharedVaultWarning(),
    observeItemById(shareId = shareId, itemId = itemId, userId = userId)
) { hasShownItemInSharedVaultWarning, item ->
    !hasShownItemInSharedVaultWarning && (item?.shareCount ?: 0) > 0
}.catch { emit(false) }
    .onStart { emit(false) }

fun persistDefaultVaultAndFolder(
    scope: CoroutineScope,
    shareUiState: StateFlow<ShareUiState>,
    setDefaultVault: SetDefaultVault,
    tag: String
) {
    scope.launch {
        withContext(NonCancellable) {
            val currentState = shareUiState.value as? ShareUiState.Success ?: return@withContext
            val shareId = currentState.currentVault.vault.shareId
            val folderId = currentState.selectedFolder?.id
            setDefaultVault(shareId, folderId)
                .onFailure { e ->
                    PassLogger.w(tag, e)
                    PassLogger.w(tag, "Failed to persist default vault/folder")
                }
        }
    }
}

enum class DialogWarningType {
    None,
    SharedVault,
    SharedItem
}

