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
import proton.android.pass.data.api.usecases.ClearPin
import proton.android.pass.data.impl.fakes.FakeLocalAppLockTypeDataSource
import proton.android.pass.data.impl.local.AppLockTypeRecord
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.AppLockTimePreference
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.preferences.UserPreferencesRepository
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SetAppLockTypeImplTest {

    private class RecordingClearPin : ClearPin {
        var called = false
        override suspend fun invoke() { called = true }
    }

    private fun instance(
        dataSource: FakeLocalAppLockTypeDataSource = FakeLocalAppLockTypeDataSource(),
        repository: UserPreferencesRepository = FakePreferenceRepository(),
        clearPin: ClearPin = RecordingClearPin()
    ) = SetAppLockTypeImpl(dataSource, repository, clearPin)

    @Test
    fun `setting pin stores type, enables proto, keeps pin file`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource()
        val repo = FakePreferenceRepository()
        val clearPin = RecordingClearPin()

        instance(ds, repo, clearPin).invoke(AppLockTypePreference.Pin)

        assertEquals(listOf(AppLockTypePreference.Pin), ds.stored.map { it.first })
        assertEquals(AppLockState.Enabled, repo.getAppLockState().first())
        assertEquals(AppLockTypePreference.Pin, repo.getAppLockTypePreference().first())
        assertTrue(!clearPin.called)
    }

    @Test
    fun `setting biometrics clears pin file`() = runTest {
        val clearPin = RecordingClearPin()

        instance(clearPin = clearPin).invoke(AppLockTypePreference.Biometrics)

        assertTrue(clearPin.called)
    }

    @Test
    fun `setting none disables proto and clears pin file`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource()
        val repo = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Enabled)
            setAppLockTypePreference(AppLockTypePreference.Pin)
        }
        val clearPin = RecordingClearPin()

        instance(ds, repo, clearPin).invoke(AppLockTypePreference.None)

        assertEquals(AppLockState.Disabled, repo.getAppLockState().first())
        assertEquals(AppLockTypePreference.None, repo.getAppLockTypePreference().first())
        assertTrue(clearPin.called)
    }

    @Test
    fun `aborts without touching prefs or pin when durable store write fails`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply { storeResult = Result.failure(RuntimeException("disk")) }
        val repo = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Disabled)
            setAppLockTypePreference(AppLockTypePreference.None)
        }
        val clearPin = RecordingClearPin()

        val result = instance(ds, repo, clearPin).invoke(AppLockTypePreference.Biometrics)

        assertTrue(result.isFailure)
        assertTrue(!clearPin.called)
        assertEquals(AppLockState.Disabled, repo.getAppLockState().first())
        assertEquals(AppLockTypePreference.None, repo.getAppLockTypePreference().first())
    }

    @Test
    fun `disabling keeps the session locked when the durable delete fails`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply {
            record = AppLockTypeRecord.Valid(AppLockTypePreference.Pin, AppLockTimePreference.InTwoMinutes)
            storeResult = Result.failure(RuntimeException("disk"))
        }
        val repo = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Enabled)
            setAppLockTypePreference(AppLockTypePreference.Pin)
        }
        val clearPin = RecordingClearPin()

        val result = instance(ds, repo, clearPin).invoke(AppLockTypePreference.None)

        assertTrue(result.isFailure)
        assertEquals(AppLockState.Enabled, repo.getAppLockState().first())
        assertEquals(AppLockTypePreference.Pin, repo.getAppLockTypePreference().first())
        assertEquals(
            AppLockTypeRecord.Valid(AppLockTypePreference.Pin, AppLockTimePreference.InTwoMinutes),
            ds.read()
        )
        assertTrue(!clearPin.called)
    }

    @Test
    fun `disabling does not clear the pin when a proto write fails`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply {
            record = AppLockTypeRecord.Valid(AppLockTypePreference.Pin, AppLockTimePreference.InTwoMinutes)
        }
        val backing = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Enabled)
            setAppLockTypePreference(AppLockTypePreference.Pin)
        }
        val repo = object : UserPreferencesRepository by backing {
            override fun setAppLockTypePreference(preference: AppLockTypePreference) =
                Result.failure<Unit>(RuntimeException("boom"))
        }
        val clearPin = RecordingClearPin()

        val result = instance(ds, repo, clearPin).invoke(AppLockTypePreference.None)

        assertTrue(result.isFailure)
        assertTrue(!clearPin.called)
    }

    @Test
    fun `state not enabled when type write fails`() = runTest {
        val repo = object : UserPreferencesRepository by FakePreferenceRepository() {
            override fun setAppLockTypePreference(preference: AppLockTypePreference) =
                Result.failure<Unit>(RuntimeException("boom"))
        }

        instance(repository = repo).invoke(AppLockTypePreference.Pin)

        assertEquals(AppLockState.Disabled, repo.getAppLockState().first())
    }
}
