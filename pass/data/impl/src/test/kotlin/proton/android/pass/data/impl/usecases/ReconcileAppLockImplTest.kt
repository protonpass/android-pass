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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.data.api.usecases.ClearPin
import proton.android.pass.data.api.usecases.ReconcileAppLockResult
import proton.android.pass.data.impl.fakes.FakeLocalAppLockTypeDataSource
import proton.android.pass.data.impl.local.AppLockTypeRecord
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.AppLockTimePreference
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.UserPreferencesRepository
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReconcileAppLockImplTest {

    private class NoopClearPin : ClearPin {
        override suspend fun invoke() = Unit
    }

    private class FailingWriteRepository(
        private val delegate: FakePreferenceRepository,
        private val failLockType: Boolean = false,
        private val failLockState: Boolean = false,
        private val failHasAuthenticated: Boolean = false
    ) : UserPreferencesRepository by delegate {

        override fun setAppLockTypePreference(preference: AppLockTypePreference): Result<Unit> = if (failLockType) {
            Result.failure(IllegalStateException("write failed"))
        } else {
            delegate.setAppLockTypePreference(preference)
        }

        override fun setAppLockState(state: AppLockState): Result<Unit> = if (failLockState) {
            Result.failure(IllegalStateException("write failed"))
        } else {
            delegate.setAppLockState(state)
        }

        override fun setHasAuthenticated(state: HasAuthenticated): Result<Unit> = if (failHasAuthenticated) {
            Result.failure(IllegalStateException("write failed"))
        } else {
            delegate.setHasAuthenticated(state)
        }
    }

    private fun instance(
        ds: FakeLocalAppLockTypeDataSource,
        repo: UserPreferencesRepository,
        internalSettings: FakeInternalSettingsRepository = FakeInternalSettingsRepository()
    ) = ReconcileAppLockImpl(ds, repo, internalSettings)

    @Test
    fun `valid record realigns a lost proto`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply {
            record = AppLockTypeRecord.Valid(AppLockTypePreference.Biometrics, AppLockTimePreference.InFiveMinutes)
        }
        val repo = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Disabled)
            setAppLockTypePreference(AppLockTypePreference.None)
            setAppLockTimePreference(AppLockTimePreference.InTwoMinutes)
        }

        val result = instance(ds, repo).invoke()

        assertEquals(ReconcileAppLockResult.Ok, result)
        assertEquals(AppLockState.Enabled, repo.getAppLockState().first())
        assertEquals(AppLockTypePreference.Biometrics, repo.getAppLockTypePreference().first())
        assertEquals(AppLockTimePreference.InFiveMinutes, repo.getAppLockTimePreference().first())
    }

    @Test
    fun `restoring a lost proto invalidates the surviving unlock window`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply {
            record = AppLockTypeRecord.Valid(AppLockTypePreference.Biometrics, AppLockTimePreference.InTwoMinutes)
        }
        val repo = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Disabled)
            setAppLockTypePreference(AppLockTypePreference.None)
            setHasAuthenticated(HasAuthenticated.Authenticated)
        }
        val internalSettings = FakeInternalSettingsRepository().apply {
            setLastUnlockedTime(1_000L)
        }

        val result = instance(ds, repo, internalSettings).invoke()

        assertEquals(ReconcileAppLockResult.Ok, result)
        assertEquals(None, internalSettings.getLastUnlockedTime().first())
        assertEquals(HasAuthenticated.NotAuthenticated, repo.getHasAuthenticated().first())
    }

    @Test
    fun `absent record with an intact proto requires seeding without writing`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply { record = AppLockTypeRecord.Absent }
        val repo = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Enabled)
            setAppLockTypePreference(AppLockTypePreference.Pin)
        }

        val result = instance(ds, repo).invoke()

        assertEquals(ReconcileAppLockResult.SeedRequired, result)
        assertTrue(ds.stored.isEmpty())
    }

    @Test
    fun `absent record with no lock is a no-op`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply { record = AppLockTypeRecord.Absent }
        val repo = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Disabled)
            setAppLockTypePreference(AppLockTypePreference.None)
        }

        val result = instance(ds, repo).invoke()

        assertEquals(ReconcileAppLockResult.Ok, result)
        assertTrue(ds.stored.isEmpty())
        assertEquals(AppLockState.Disabled, repo.getAppLockState().first())
    }

    @Test
    fun `absent record with password only lock is a no-op`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply { record = AppLockTypeRecord.Absent }
        val repo = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Enabled)
            setAppLockTypePreference(AppLockTypePreference.None)
        }

        val result = instance(ds, repo).invoke()

        assertEquals(ReconcileAppLockResult.Ok, result)
        assertTrue(ds.stored.isEmpty())
        assertEquals(AppLockState.Enabled, repo.getAppLockState().first())
        assertEquals(AppLockTypePreference.None, repo.getAppLockTypePreference().first())
    }

    @Test
    fun `switching a pin lock to password only survives the next reconcile`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource()
        val repo = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Enabled)
            setAppLockTypePreference(AppLockTypePreference.Pin)
            setAppLockTimePreference(AppLockTimePreference.InTwoMinutes)
            setHasAuthenticated(HasAuthenticated.Authenticated)
        }
        ds.record = AppLockTypeRecord.Valid(AppLockTypePreference.Pin, AppLockTimePreference.InTwoMinutes)

        SetPasswordOnlyLockImpl(ds, repo, NoopClearPin()).invoke()

        val result = instance(ds, repo).invoke()

        assertEquals(ReconcileAppLockResult.Ok, result)
        assertEquals(AppLockTypePreference.None, repo.getAppLockTypePreference().first())
        assertEquals(AppLockState.Enabled, repo.getAppLockState().first())
        assertEquals(HasAuthenticated.Authenticated, repo.getHasAuthenticated().first())
    }

    @Test
    fun `biometric enrollment change survives a restart`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource()
        val repo = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Enabled)
            setAppLockTypePreference(AppLockTypePreference.Biometrics)
            setAppLockTimePreference(AppLockTimePreference.InTwoMinutes)
            setHasAuthenticated(HasAuthenticated.Authenticated)
        }
        ds.record = AppLockTypeRecord.Valid(AppLockTypePreference.Biometrics, AppLockTimePreference.InTwoMinutes)

        SetPasswordOnlyLockImpl(ds, repo, NoopClearPin()).invoke()

        assertEquals(AppLockTypeRecord.Absent, ds.read())

        val result = instance(ds, repo).invoke()

        assertEquals(ReconcileAppLockResult.Ok, result)
        assertEquals(AppLockTypePreference.None, repo.getAppLockTypePreference().first())
        assertEquals(AppLockState.Enabled, repo.getAppLockState().first())
        assertEquals(HasAuthenticated.Authenticated, repo.getHasAuthenticated().first())
    }

    @Test
    fun `failing to restore the lock type requires reauth`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply {
            record = AppLockTypeRecord.Valid(AppLockTypePreference.Biometrics, AppLockTimePreference.InTwoMinutes)
        }
        val delegate = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Disabled)
            setAppLockTypePreference(AppLockTypePreference.None)
        }
        val repo = FailingWriteRepository(delegate, failLockType = true)

        val result = instance(ds, repo).invoke()

        assertEquals(ReconcileAppLockResult.RequireReauth, result)
        assertEquals(AppLockTypePreference.None, repo.getAppLockTypePreference().first())
        assertEquals(AppLockState.Disabled, repo.getAppLockState().first())
    }

    @Test
    fun `failing to restore the lock state requires reauth`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply {
            record = AppLockTypeRecord.Valid(AppLockTypePreference.Biometrics, AppLockTimePreference.InTwoMinutes)
        }
        val delegate = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Disabled)
            setAppLockTypePreference(AppLockTypePreference.Biometrics)
            setAppLockTimePreference(AppLockTimePreference.InTwoMinutes)
        }
        val repo = FailingWriteRepository(delegate, failLockState = true)

        val result = instance(ds, repo).invoke()

        assertEquals(ReconcileAppLockResult.RequireReauth, result)
        assertEquals(AppLockState.Disabled, repo.getAppLockState().first())
    }

    @Test
    fun `failing to invalidate the unlock window requires reauth`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply {
            record = AppLockTypeRecord.Valid(AppLockTypePreference.Biometrics, AppLockTimePreference.InTwoMinutes)
        }
        val delegate = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Disabled)
            setAppLockTypePreference(AppLockTypePreference.None)
            setHasAuthenticated(HasAuthenticated.Authenticated)
        }
        val repo = FailingWriteRepository(delegate, failHasAuthenticated = true)

        val result = instance(ds, repo).invoke()

        assertEquals(ReconcileAppLockResult.RequireReauth, result)
        assertEquals(AppLockState.Enabled, repo.getAppLockState().first())
        assertEquals(HasAuthenticated.Authenticated, repo.getHasAuthenticated().first())
    }

    @Test
    fun `corrupted record requires reauth and drops the unusable file`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply { record = AppLockTypeRecord.Corrupted }
        val repo = FakePreferenceRepository()

        val result = instance(ds, repo).invoke()

        assertEquals(ReconcileAppLockResult.RequireReauth, result)
        assertEquals(AppLockTypeRecord.Absent, ds.read())
    }

    @Test
    fun `corrupted record still requires reauth when the drop fails`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply {
            record = AppLockTypeRecord.Corrupted
            storeResult = Result.failure(IllegalStateException("write failed"))
        }
        val repo = FakePreferenceRepository()

        val result = instance(ds, repo).invoke()

        assertEquals(ReconcileAppLockResult.RequireReauth, result)
        assertEquals(AppLockTypeRecord.Corrupted, ds.read())
    }
}
