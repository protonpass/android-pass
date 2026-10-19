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

package proton.android.pass.features.settings

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.FakeDeviceSettingsRepository
import proton.android.pass.autofill.fakes.FakeAutofillManager
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.data.fakes.repositories.FakeAssetLinkRepository
import proton.android.pass.data.fakes.usecases.FakeGetPrimaryUserId
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.fakes.usecases.FakeInitialWorkerLauncher
import proton.android.pass.data.fakes.usecases.FakeItemSyncStatusRepository
import proton.android.pass.data.fakes.usecases.FakeObservePrimaryUserId
import proton.android.pass.data.fakes.usecases.FakePerformSync
import proton.android.pass.data.fakes.usecases.attachments.FakeAttachmentDownloadScheduler
import proton.android.pass.data.fakes.usecases.attachments.FakeEnableAllOfflineAttachments
import proton.android.pass.data.fakes.usecases.attachments.FakeSetSharedItemsOfflineAttachments
import proton.android.pass.image.fakes.FakeClearIconCache
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FakeFeatureFlagsPreferenceRepository
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.telemetry.fakes.FakeCanConfigureTelemetry
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.MainDispatcherRule

class SettingsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var performSync: FakePerformSync
    private lateinit var instance: SettingsViewModel

    @Before
    fun setUp() {
        performSync = FakePerformSync()
        instance = SettingsViewModel(
            preferencesRepository = FakePreferenceRepository(),
            snackbarDispatcher = FakeSnackbarDispatcher(),
            performSync = performSync,
            observePrimaryUserId = FakeObservePrimaryUserId(),
            getPrimaryUserId = FakeGetPrimaryUserId(),
            clearIconCache = FakeClearIconCache(),
            deviceSettingsRepository = FakeDeviceSettingsRepository(),
            canConfigureTelemetry = FakeCanConfigureTelemetry(),
            initialWorkerLauncher = FakeInitialWorkerLauncher(),
            assetLinkRepository = FakeAssetLinkRepository(),
            enableAllOfflineAttachments = FakeEnableAllOfflineAttachments(),
            setSharedItemsOfflineAttachments = FakeSetSharedItemsOfflineAttachments(),
            attachmentDownloadScheduler = FakeAttachmentDownloadScheduler(),
            featureFlagsRepository = FakeFeatureFlagsPreferenceRepository(),
            getUserPlan = FakeGetUserPlan(),
            autofillManager = FakeAutofillManager(),
            syncStatusRepository = FakeItemSyncStatusRepository(),
            telemetryManager = FakeTelemetryManager(),
            appDispatchers = FakeAppDispatchers()
        )
    }

    @Test
    fun `second force sync while first is still in flight does not invoke performSync again`() = runTest {
        performSync.gate = CompletableDeferred()

        instance.onForceSync()
        runCurrent()
        instance.onForceSync()
        runCurrent()

        assertThat(performSync.invocations).hasSize(1)

        performSync.gate?.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun `force sync runs again after the previous one completed`() = runTest {
        performSync.gate = CompletableDeferred()

        instance.onForceSync()
        runCurrent()
        performSync.gate?.complete(Unit)
        advanceUntilIdle()

        instance.onForceSync()
        advanceUntilIdle()

        assertThat(performSync.invocations).hasSize(2)
    }

    @Test
    fun `force sync guard is released when performSync fails so a later call runs`() = runTest {
        performSync.setResult(Result.failure(IllegalStateException("sync failed")))
        performSync.gate = CompletableDeferred()

        instance.onForceSync()
        runCurrent()
        performSync.gate?.complete(Unit)
        advanceUntilIdle()

        instance.onForceSync()
        advanceUntilIdle()

        assertThat(performSync.invocations).hasSize(2)
    }
}
