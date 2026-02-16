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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.data.api.usecases.RefreshGroupInvites
import proton.android.pass.data.api.usecases.RefreshUserInvites
import proton.android.pass.data.api.usecases.SyncUserEvents
import proton.android.pass.data.api.usecases.folders.RefreshFolders
import proton.android.pass.data.api.usecases.simplelogin.SyncSimpleLoginPendingAliases
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class PerformSyncImpl @Inject constructor(
    private val applyPendingEvents: ApplyPendingEvents,
    private val refreshUserInvites: RefreshUserInvites,
    private val refreshGroupInvites: RefreshGroupInvites,
    private val syncPendingAliases: SyncSimpleLoginPendingAliases,
    private val syncUserEvents: SyncUserEvents,
    private val shareRepository: ShareRepository,
    private val refreshFolders: RefreshFolders,
    private val featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository
) : PerformSync {

    override suspend fun invoke(userId: UserId, forceSync: Boolean) {
        PassLogger.i(TAG, "Performing sync for $userId started (forceSync=$forceSync)")

        performSyncWithPendingEvents(userId, forceSync)

        PassLogger.i(TAG, "Performing sync for $userId finished")
    }

    private suspend fun performSyncWithPendingEvents(userId: UserId, forceSync: Boolean) = coroutineScope {
        val isUserEventsEnabled: Boolean =
            featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.PASS_USER_EVENTS_V1).first()
        val isFoldersEnabled: Boolean =
            featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.PASS_FOLDERS).first()
        val isGroupSharingEnabled =
            featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.PASS_GROUP_SHARE).first()

        val results = if (isUserEventsEnabled) {
            listOf(performSyncUserEvents(userId, forceSync))
        } else {
            val tasks = buildList {
                add(async { performPendingEvents(userId, forceSync) })
                add(async { performUserRefreshInvites(userId) })
                if (isGroupSharingEnabled) {
                    add(async { performGroupRefreshInvites(userId) })
                }
                add(async { syncPendingSlAliases(userId, forceSync) })
                if (isFoldersEnabled) {
                    add(async { refreshAllVaultFolders(userId) })
                }
            }
            tasks.awaitAll()
        }

        results.firstOrNull { it.isFailure }?.let { result ->
            result.exceptionOrNull()?.let { error ->
                PassLogger.w(TAG, "Performing sync error: ${error.message}")
            }
        }
    }

    private suspend fun performPendingEvents(userId: UserId, forceSync: Boolean): Result<Unit> = safeRunCatching {
        withTimeout(2.minutes) {
            applyPendingEvents(userId, forceSync)
            PassLogger.i(TAG, "Pending events for $userId finished")
        }
    }.onFailure { error ->
        PassLogger.w(TAG, "Pending events for $userId error: ${error.message}")
    }

    private suspend fun performUserRefreshInvites(userId: UserId): Result<Unit> = safeRunCatching {
        withTimeout(2.minutes) {
            refreshUserInvites(userId)
            PassLogger.i(TAG, "Refresh user invites for $userId finished")
        }
    }.onFailure { error ->
        PassLogger.w(TAG, "Refresh user invites for $userId error: ${error.message}")
    }

    private suspend fun performGroupRefreshInvites(userId: UserId): Result<Unit> = safeRunCatching {
        withTimeout(2.minutes) {
            refreshGroupInvites(userId)
            PassLogger.i(TAG, "Refresh group invites for $userId finished")
        }
    }.onFailure { error ->
        PassLogger.w(TAG, "Refresh group invites for $userId error: ${error.message}")
    }

    private suspend fun syncPendingSlAliases(userId: UserId, forceSync: Boolean): Result<Unit> = safeRunCatching {
        withTimeout(2.minutes) {
            syncPendingAliases(userId, forceSync)
            PassLogger.i(TAG, "Pending SL aliases sync for $userId finished")
        }
    }.onFailure { error ->
        PassLogger.w(TAG, "Pending SL aliases sync for $userId error: ${error.message}")
    }

    private suspend fun performSyncUserEvents(userId: UserId, forceSync: Boolean): Result<Unit> = runCatching {
        withTimeout(2.minutes) {
            syncUserEvents(userId, forceSync)
            PassLogger.i(TAG, "User events sync for $userId finished")
        }
    }.onFailure { error ->
        PassLogger.w(TAG, "User events sync for $userId error: ${error.message}")
    }

    private suspend fun refreshAllVaultFolders(userId: UserId): Result<Unit> = safeRunCatching {
        val vaultShareIds = shareRepository.observeSharesByType(
            userId = userId,
            shareType = ShareType.Vault,
            includeHidden = true
        ).first().map { it.id }.toSet()
        refreshFoldersForShares(userId, vaultShareIds).getOrThrow()
    }.onFailure { error ->
        PassLogger.w(TAG, "Refresh all folders for $userId error: ${error.message}")
    }

    private suspend fun refreshFoldersForShares(userId: UserId, shareIds: Set<ShareId>): Result<Unit> =
        safeRunCatching {
            shareIds.forEach { shareId ->
                refreshFolders(userId, shareId)
            }
            PassLogger.i(TAG, "Folders refreshed for ${shareIds.size} shares")
        }.onFailure { error ->
            PassLogger.w(TAG, "Refresh folders for selected shares for $userId error: ${error.message}")
        }

    private companion object {

        private const val TAG = "PerformSyncImpl"

    }

}
