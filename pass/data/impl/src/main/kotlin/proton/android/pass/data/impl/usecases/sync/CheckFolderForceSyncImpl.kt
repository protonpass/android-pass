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

package proton.android.pass.data.impl.usecases.sync

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.repositories.SyncReason
import proton.android.pass.data.api.usecases.PerformSync
import proton.android.pass.data.api.usecases.folders.FolderPresence
import proton.android.pass.data.api.usecases.folders.HasAnyFolders
import proton.android.pass.data.api.usecases.sync.CheckFolderForceSync
import proton.android.pass.data.impl.extensions.isForceSyncFoldersEnabled
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.ForceSyncFolderPreference
import proton.android.pass.preferences.InternalSettingsRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes

/**
 * Repairs clients that missed folder events while running a pre-folders build: the events were
 * dropped but the event token advanced past them, so a full download is the only way to recover.
 */
@Singleton
class CheckFolderForceSyncImpl @Inject constructor(
    private val internalSettingsRepository: InternalSettingsRepository,
    private val featureFlagsRepository: FeatureFlagsPreferencesRepository,
    private val hasAnyFolders: HasAnyFolders,
    private val shareRepository: ShareRepository,
    private val itemSyncStatusRepository: ItemSyncStatusRepository,
    private val performSync: PerformSync,
    private val clock: Clock,
    private val appDispatchers: AppDispatchers
) : CheckFolderForceSync {

    private val lastLoggedDecisions = ConcurrentHashMap<UserId, Decision>()
    private val userMutexes = ConcurrentHashMap<UserId, Mutex>()

    override suspend fun invoke(userId: UserId, syncMode: SyncMode) = withContext(appDispatchers.io) {
        userMutexes.computeIfAbsent(userId) { Mutex() }.withLock {
            runIfOwed(userId, syncMode)
        }
    }

    private suspend fun runIfOwed(userId: UserId, syncMode: SyncMode) {
        val preference = internalSettingsRepository.getForceSyncFolderPreference(userId)
        val nowMs = clock.now().toEpochMilliseconds()
        val decision = decide(userId, preference, nowMs)

        logDecision(userId, decision, preference)

        when (decision) {
            Decision.AlreadyDone -> Unit

            Decision.FlagDisabled -> Unit

            Decision.SyncInProgress -> Unit

            Decision.TooSoon -> Unit

            Decision.Exhausted -> {
                internalSettingsRepository.updateForceSyncFolderPreference(userId) { current ->
                    current.copy(done = true)
                }
            }

            Decision.Proceed -> runCheck(userId, nowMs, syncMode)
        }
    }

    private fun logDecision(
        userId: UserId,
        decision: Decision,
        preference: ForceSyncFolderPreference
    ) {
        if (lastLoggedDecisions.put(userId, decision) == decision) return

        val message = when (decision) {
            Decision.AlreadyDone -> "already settled"
            Decision.FlagDisabled -> "skipped, feature flag disabled"
            Decision.SyncInProgress -> "skipped, another sync is already running"
            Decision.TooSoon -> "skipped, too soon after attempt ${preference.attempts}"
            Decision.Exhausted -> "exhausted after ${preference.attempts} attempts"
            Decision.Proceed -> "checking, attempt ${preference.attempts + 1} of $MAX_ATTEMPTS"
        }.let { reason -> "Folders force sync $reason" }

        if (decision == Decision.Exhausted) {
            PassLogger.w(TAG, message)
        } else {
            PassLogger.i(TAG, message)
        }
    }

    private suspend fun decide(
        userId: UserId,
        preference: ForceSyncFolderPreference,
        nowMs: Long
    ): Decision = when {
        preference.done -> Decision.AlreadyDone
        itemSyncStatusRepository.observeSyncState().first().isSyncing -> Decision.SyncInProgress
        preference.lastAttemptAtMs > 0 &&
            nowMs - preference.lastAttemptAtMs < MIN_ATTEMPT_INTERVAL.inWholeMilliseconds ->
            Decision.TooSoon

        !featureFlagsRepository.isForceSyncFoldersEnabled(userId) -> Decision.FlagDisabled
        preference.attempts >= MAX_ATTEMPTS -> Decision.Exhausted
        else -> Decision.Proceed
    }

    private suspend fun runCheck(
        userId: UserId,
        nowMs: Long,
        syncMode: SyncMode
    ) {
        when (folderPresence(userId)) {
            FolderPresence.NoFolders -> {
                PassLogger.i(TAG, "No folders found, folders force sync not needed")
                internalSettingsRepository.updateForceSyncFolderPreference(userId) {
                    ForceSyncFolderPreference.Initial.copy(done = true)
                }
            }

            FolderPresence.Unknown -> {
                // Costs no attempt: offline or a failing endpoint must not exhaust the budget
                PassLogger.i(TAG, "Folder presence unknown, retrying later")
                throttleUntilNextInterval(userId, nowMs)
            }

            FolderPresence.HasFolders -> {
                PassLogger.i(TAG, "Folders found, running folders force sync (syncMode=$syncMode)")
                // Consumed before the sync, so a process death mid-download still counts
                consumeAttempt(userId, nowMs)
                performSync(
                    userId = userId,
                    forceSync = true,
                    trigger = TRIGGER,
                    syncReason = SyncReason.FolderRepair,
                    syncMode = syncMode
                )
            }
        }
    }

    private suspend fun folderPresence(userId: UserId): FolderPresence = safeRunCatching {
        val shareIds = shareRepository.observeAllShares(userId, includeHidden = true)
            .first()
            .map { share -> share.id }
            .toSet()

        hasAnyFolders(userId, shareIds)
    }.onFailure { error ->
        PassLogger.w(TAG, "Folder presence check failed")
        PassLogger.w(TAG, error)
    }.getOrDefault(FolderPresence.Unknown)

    private enum class Decision {
        AlreadyDone,
        FlagDisabled,
        SyncInProgress,
        Exhausted,
        TooSoon,
        Proceed
    }

    private suspend fun consumeAttempt(userId: UserId, nowMs: Long) =
        internalSettingsRepository.updateForceSyncFolderPreference(userId) { current ->
            current.copy(
                attempts = current.attempts + 1,
                lastAttemptAtMs = nowMs
            )
        }

    private suspend fun throttleUntilNextInterval(userId: UserId, nowMs: Long) =
        internalSettingsRepository.updateForceSyncFolderPreference(userId) { current ->
            current.copy(lastAttemptAtMs = nowMs)
        }

    private companion object {

        private const val TAG = "CheckFolderForceSyncImpl"
        private const val TRIGGER = "force_sync_folders"

        private const val MAX_ATTEMPTS = 10
        private val MIN_ATTEMPT_INTERVAL = 30.minutes

    }
}
