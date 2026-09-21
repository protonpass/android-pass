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

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.repositories.SyncReason
import proton.android.pass.data.api.usecases.folders.FolderPresence
import proton.android.pass.data.fakes.usecases.FakePerformSync
import proton.android.pass.data.fakes.usecases.folders.FakeHasAnyFolders
import proton.android.pass.data.fakes.usecases.FakeItemSyncStatusRepository
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.preferences.FakeFeatureFlagsPreferenceRepository
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.ForceSyncFolderPreference
import proton.android.pass.test.FixedClock
import proton.android.pass.test.domain.ShareTestFactory
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

internal class CheckFolderForceSyncImplTest {

    private val testScheduler = TestCoroutineScheduler()

    private lateinit var internalSettingsRepository: FakeInternalSettingsRepository
    private lateinit var featureFlagsRepository: FakeFeatureFlagsPreferenceRepository
    private lateinit var hasAnyFolders: FakeHasAnyFolders
    private lateinit var shareRepository: FakeShareRepository
    private lateinit var itemSyncStatusRepository: FakeItemSyncStatusRepository
    private lateinit var performSync: FakePerformSync
    private lateinit var clock: FixedClock
    private lateinit var instance: CheckFolderForceSyncImpl

    @Before
    fun setup() {
        internalSettingsRepository = FakeInternalSettingsRepository()
        featureFlagsRepository = FakeFeatureFlagsPreferenceRepository().apply {
            set(FeatureFlag.PASS_FORCE_SYNC_FOLDERS, true)
        }
        hasAnyFolders = FakeHasAnyFolders()
        shareRepository = FakeShareRepository().apply {
            emitObserveShares(Result.success(listOf(ShareTestFactory.Vault.create(id = "share-1"))))
        }
        itemSyncStatusRepository = FakeItemSyncStatusRepository().apply {
            tryEmit(ItemSyncStatus.SyncNotStarted)
        }
        performSync = FakePerformSync()
        clock = FixedClock(Instant.fromEpochMilliseconds(NOW_MS))
        instance = CheckFolderForceSyncImpl(
            internalSettingsRepository = internalSettingsRepository,
            featureFlagsRepository = featureFlagsRepository,
            hasAnyFolders = hasAnyFolders,
            shareRepository = shareRepository,
            itemSyncStatusRepository = itemSyncStatusRepository,
            performSync = performSync,
            clock = clock,
            appDispatchers = FakeAppDispatchers.withTestDispatcher(UnconfinedTestDispatcher(testScheduler))
        )
    }

    @Test
    fun `does nothing while another sync is already running`() = runTest(testScheduler) {
        hasAnyFolders.setResult(FolderPresence.HasFolders)
        itemSyncStatusRepository.tryEmit(ItemSyncStatus.SyncStarted)

        instance(USER_ID)

        assertThat(hasAnyFolders.invocations).isEmpty()
        assertThat(performSync.invocations).isEmpty()
        assertThat(preference().done).isFalse()
        assertThat(preference().attempts).isEqualTo(0)
        assertThat(featureFlagsRepository.awaitResolvedInvocations).isEmpty()
    }

    @Test
    fun `does not check the feature flag once the repair is already done`() = runTest(testScheduler) {
        setPreference(ForceSyncFolderPreference.Initial.copy(done = true))

        instance(USER_ID)

        assertThat(featureFlagsRepository.awaitResolvedInvocations).isEmpty()
    }

    @Test
    fun `does not check the feature flag within the minimum attempt interval`() = runTest(testScheduler) {
        setPreference(
            ForceSyncFolderPreference.Initial.copy(
                attempts = 1,
                lastAttemptAtMs = NOW_MS
            )
        )

        instance(USER_ID)

        assertThat(featureFlagsRepository.awaitResolvedInvocations).isEmpty()
    }

    @Test
    fun `checks the feature flag once the cheap local guards pass`() = runTest(testScheduler) {
        hasAnyFolders.setResult(FolderPresence.HasFolders)

        instance(USER_ID)

        assertThat(featureFlagsRepository.awaitResolvedInvocations)
            .containsExactly(FeatureFlag.PASS_FORCE_SYNC_FOLDERS)
    }

    @Test
    fun `a disabled flag does not settle the repair even when attempts are exhausted`() = runTest(testScheduler) {
        featureFlagsRepository.set(FeatureFlag.PASS_FORCE_SYNC_FOLDERS, false)
        setPreference(
            ForceSyncFolderPreference.Initial.copy(
                attempts = MAX_ATTEMPTS,
                lastAttemptAtMs = NOW_MS - MIN_ATTEMPT_INTERVAL.inWholeMilliseconds
            )
        )

        instance(USER_ID)

        assertThat(performSync.invocations).isEmpty()
        assertThat(preference().done).isFalse()
    }

    @Test
    fun `does nothing while the feature flag is disabled`() = runTest(testScheduler) {
        featureFlagsRepository.set(FeatureFlag.PASS_FORCE_SYNC_FOLDERS, false)
        hasAnyFolders.setResult(FolderPresence.HasFolders)

        instance(USER_ID)

        assertThat(hasAnyFolders.invocations).isEmpty()
        assertThat(performSync.invocations).isEmpty()
        assertThat(preference().done).isFalse()
        assertThat(preference().attempts).isEqualTo(0)
    }

    @Test
    fun `is disabled by default`() = runTest(testScheduler) {
        featureFlagsRepository = FakeFeatureFlagsPreferenceRepository()
        instance = CheckFolderForceSyncImpl(
            internalSettingsRepository = internalSettingsRepository,
            featureFlagsRepository = featureFlagsRepository,
            hasAnyFolders = hasAnyFolders,
            shareRepository = shareRepository,
            itemSyncStatusRepository = itemSyncStatusRepository,
            performSync = performSync,
            clock = clock,
            appDispatchers = FakeAppDispatchers.withTestDispatcher(UnconfinedTestDispatcher(testScheduler))
        )
        hasAnyFolders.setResult(FolderPresence.HasFolders)

        instance(USER_ID)

        assertThat(performSync.invocations).isEmpty()
    }

    @Test
    fun `runs once the feature flag is enabled`() = runTest(testScheduler) {
        featureFlagsRepository.set(FeatureFlag.PASS_FORCE_SYNC_FOLDERS, false)
        hasAnyFolders.setResult(FolderPresence.HasFolders)
        instance(USER_ID)

        featureFlagsRepository.set(FeatureFlag.PASS_FORCE_SYNC_FOLDERS, true)
        instance(USER_ID)

        assertThat(performSync.invocations).hasSize(1)
    }

    @Test
    fun `does nothing once the repair is done`() = runTest(testScheduler) {
        setPreference(ForceSyncFolderPreference.Initial.copy(done = true))

        instance(USER_ID)

        assertThat(hasAnyFolders.invocations).isEmpty()
        assertThat(performSync.invocations).isEmpty()
    }

    @Test
    fun `runs the force sync when the user has folders`() = runTest(testScheduler) {
        hasAnyFolders.setResult(FolderPresence.HasFolders)

        instance(USER_ID)

        assertThat(performSync.invocations).hasSize(1)
        assertThat(performSync.invocations.first().forceSync).isTrue()
        assertThat(performSync.invocations.first().syncReason).isEqualTo(SyncReason.FolderRepair)
        assertThat(performSync.invocations.first().syncMode).isEqualTo(SyncMode.ShownToUser)
        // Settled by the worker once the download finishes
        assertThat(preference().done).isFalse()
    }

    @Test
    fun `runs the repair silently when asked for a background sync`() = runTest(testScheduler) {
        hasAnyFolders.setResult(FolderPresence.HasFolders)

        instance(USER_ID, SyncMode.Background)

        assertThat(performSync.invocations).hasSize(1)
        assertThat(performSync.invocations.first().forceSync).isTrue()
        assertThat(performSync.invocations.first().syncMode).isEqualTo(SyncMode.Background)
    }

    @Test
    fun `settles the repair when the user has no folders`() = runTest(testScheduler) {
        hasAnyFolders.setResult(FolderPresence.NoFolders)

        instance(USER_ID)

        assertThat(performSync.invocations).isEmpty()
        assertThat(preference().done).isTrue()
    }

    @Test
    fun `unknown folder presence does not settle the repair`() = runTest(testScheduler) {
        hasAnyFolders.setResult(FolderPresence.Unknown)

        instance(USER_ID)

        assertThat(performSync.invocations).isEmpty()
        assertThat(preference().done).isFalse()
        assertThat(preference().attempts).isEqualTo(0)
        assertThat(preference().lastAttemptAtMs).isEqualTo(NOW_MS)
    }

    @Test
    fun `consumes the attempt when the check produces an answer`() = runTest(testScheduler) {
        hasAnyFolders.setResult(FolderPresence.HasFolders)

        instance(USER_ID)

        assertThat(preference().attempts).isEqualTo(1)
        assertThat(preference().lastAttemptAtMs).isEqualTo(NOW_MS)
    }

    @Test
    fun `a cancelled check leaves no state behind`() = runTest(testScheduler) {
        hasAnyFolders.setResult(FolderPresence.HasFolders)
        hasAnyFolders.setThrowable(CancellationException("cancelled mid-check"))

        runCatching { instance(USER_ID) }

        assertThat(performSync.invocations).isEmpty()
        assertThat(preference().attempts).isEqualTo(0)
        assertThat(preference().lastAttemptAtMs).isEqualTo(0)
    }

    @Test
    fun `retries immediately after a cancelled check instead of waiting out the interval`() = runTest(testScheduler) {
        hasAnyFolders.setResult(FolderPresence.HasFolders)
        hasAnyFolders.setThrowable(CancellationException("cancelled mid-check"))
        runCatching { instance(USER_ID) }

        hasAnyFolders.setThrowable(null)

        instance(USER_ID)

        assertThat(performSync.invocations).hasSize(1)
        assertThat(preference().attempts).isEqualTo(1)
    }

    @Test
    fun `does not spend an attempt when the check fails`() = runTest(testScheduler) {
        hasAnyFolders.setResult(FolderPresence.HasFolders)
        hasAnyFolders.setThrowable(IllegalStateException("shares unavailable"))

        instance(USER_ID)

        assertThat(performSync.invocations).isEmpty()
        assertThat(preference().done).isFalse()
        assertThat(preference().attempts).isEqualTo(0)
        assertThat(preference().lastAttemptAtMs).isEqualTo(NOW_MS)
    }

    @Test
    fun `a failed check does not stop a later one from repairing`() = runTest(testScheduler) {
        hasAnyFolders.setResult(FolderPresence.HasFolders)
        hasAnyFolders.setThrowable(IllegalStateException("shares unavailable"))

        instance(USER_ID)

        assertThat(performSync.invocations).isEmpty()
        assertThat(preference().attempts).isEqualTo(0)

        hasAnyFolders.setThrowable(null)
        clock.updateInstant(
            Instant.fromEpochMilliseconds(NOW_MS + MIN_ATTEMPT_INTERVAL.inWholeMilliseconds)
        )

        instance(USER_ID)

        assertThat(performSync.invocations).hasSize(1)
        assertThat(preference().attempts).isEqualTo(1)
        assertThat(preference().done).isFalse()
    }

    @Test
    fun `never settles the repair on a client that can never answer the check`() = runTest(testScheduler) {
        hasAnyFolders.setResult(FolderPresence.HasFolders)
        hasAnyFolders.setThrowable(IllegalStateException("shares unavailable"))

        repeat(MAX_ATTEMPTS + 1) { elapsedIntervals ->
            clock.updateInstant(
                Instant.fromEpochMilliseconds(
                    NOW_MS + elapsedIntervals * MIN_ATTEMPT_INTERVAL.inWholeMilliseconds
                )
            )

            instance(USER_ID)
        }

        assertThat(performSync.invocations).isEmpty()
        assertThat(hasAnyFolders.invocations).hasSize(MAX_ATTEMPTS + 1)
        assertThat(preference().attempts).isEqualTo(0)
        assertThat(preference().done).isFalse()
    }

    @Test
    fun `does not settle an exhausted repair while a sync is still running`() = runTest(testScheduler) {
        setPreference(
            ForceSyncFolderPreference.Initial.copy(
                attempts = MAX_ATTEMPTS,
                lastAttemptAtMs = NOW_MS - MIN_ATTEMPT_INTERVAL.inWholeMilliseconds
            )
        )
        itemSyncStatusRepository.tryEmit(ItemSyncStatus.SyncStarted)

        instance(USER_ID)

        assertThat(preference().done).isFalse()
    }

    @Test
    fun `does not settle an exhausted repair inside the attempt interval`() = runTest(testScheduler) {
        setPreference(
            ForceSyncFolderPreference.Initial.copy(
                attempts = MAX_ATTEMPTS,
                lastAttemptAtMs = NOW_MS
            )
        )

        instance(USER_ID)

        assertThat(preference().done).isFalse()
    }

    @Test
    fun `skips one millisecond before the minimum attempt interval`() = runTest(testScheduler) {
        hasAnyFolders.setResult(FolderPresence.HasFolders)
        setPreference(
            ForceSyncFolderPreference.Initial.copy(
                attempts = 1,
                lastAttemptAtMs = NOW_MS - (MIN_ATTEMPT_INTERVAL - 1.milliseconds).inWholeMilliseconds
            )
        )

        instance(USER_ID)

        assertThat(hasAnyFolders.invocations).isEmpty()
        assertThat(performSync.invocations).isEmpty()
        assertThat(preference().attempts).isEqualTo(1)
    }

    @Test
    fun `retries exactly on the minimum attempt interval`() = runTest(testScheduler) {
        hasAnyFolders.setResult(FolderPresence.HasFolders)
        setPreference(
            ForceSyncFolderPreference.Initial.copy(
                attempts = 1,
                lastAttemptAtMs = NOW_MS - MIN_ATTEMPT_INTERVAL.inWholeMilliseconds
            )
        )

        instance(USER_ID)

        assertThat(performSync.invocations).hasSize(1)
        assertThat(preference().attempts).isEqualTo(2)
    }

    @Test
    fun `still retries on the last allowed attempt`() = runTest(testScheduler) {
        hasAnyFolders.setResult(FolderPresence.HasFolders)
        setPreference(
            ForceSyncFolderPreference.Initial.copy(
                attempts = MAX_ATTEMPTS - 1,
                lastAttemptAtMs = NOW_MS - MIN_ATTEMPT_INTERVAL.inWholeMilliseconds
            )
        )

        instance(USER_ID)

        assertThat(performSync.invocations).hasSize(1)
        assertThat(preference().done).isFalse()
    }

    @Test
    fun `settles the repair once attempts are exhausted`() = runTest(testScheduler) {
        hasAnyFolders.setResult(FolderPresence.HasFolders)
        setPreference(
            ForceSyncFolderPreference.Initial.copy(
                attempts = MAX_ATTEMPTS,
                lastAttemptAtMs = NOW_MS - MIN_ATTEMPT_INTERVAL.inWholeMilliseconds
            )
        )

        instance(USER_ID)

        assertThat(hasAnyFolders.invocations).isEmpty()
        assertThat(performSync.invocations).isEmpty()
        assertThat(preference().done).isTrue()
    }

    @Test
    fun `does not re-arm the repair settled while the check was in flight`() = runTest(testScheduler) {
        hasAnyFolders.setResult(FolderPresence.HasFolders)
        hasAnyFolders.setOnInvoke {
            internalSettingsRepository.updateForceSyncFolderPreference(USER_ID) { current ->
                current.copy(done = true)
            }
        }

        instance(USER_ID)

        assertThat(preference().done).isTrue()
    }

    @Test
    fun `state is kept per user`() = runTest(testScheduler) {
        val otherUserId = UserId("other-user-id")
        hasAnyFolders.setResult(FolderPresence.NoFolders)

        instance(USER_ID)

        assertThat(preference().done).isTrue()
        assertThat(internalSettingsRepository.getForceSyncFolderPreference(otherUserId).done).isFalse()
    }

    private suspend fun setPreference(preference: ForceSyncFolderPreference) =
        internalSettingsRepository.updateForceSyncFolderPreference(USER_ID) { preference }

    private suspend fun preference(): ForceSyncFolderPreference =
        internalSettingsRepository.getForceSyncFolderPreference(USER_ID)

    private companion object {

        private val USER_ID = UserId("user-id")
        private const val MAX_ATTEMPTS = 10
        private val MIN_ATTEMPT_INTERVAL = 30.minutes

        private val NOW_MS = 30.days.inWholeMilliseconds

    }
}
