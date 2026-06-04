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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SetPasswordOnlyLockImplTest {

    private class RecordingClearPin : ClearPin {
        var called = false
        override suspend fun invoke() { called = true }
    }

    @Test
    fun `drops the durable record, clears the pin and keeps the lock enabled`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply {
            record = AppLockTypeRecord.Valid(AppLockTypePreference.Pin, AppLockTimePreference.InTwoMinutes)
        }
        val repo = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Enabled)
            setAppLockTypePreference(AppLockTypePreference.Pin)
            setAppLockTimePreference(AppLockTimePreference.InTwoMinutes)
        }
        val clearPin = RecordingClearPin()

        val result = SetPasswordOnlyLockImpl(ds, repo, clearPin).invoke()

        assertTrue(result.isSuccess)
        assertEquals(AppLockTypeRecord.Absent, ds.read())
        assertEquals(listOf(AppLockTypePreference.None), ds.stored.map { it.first })
        assertTrue(clearPin.called)
        assertEquals(AppLockTypePreference.None, repo.getAppLockTypePreference().first())
        assertEquals(AppLockState.Enabled, repo.getAppLockState().first())
    }

    @Test
    fun `aborts without touching prefs or pin when the durable drop fails`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply { storeResult = Result.failure(RuntimeException("disk")) }
        val repo = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Enabled)
            setAppLockTypePreference(AppLockTypePreference.Pin)
        }
        val clearPin = RecordingClearPin()

        val result = SetPasswordOnlyLockImpl(ds, repo, clearPin).invoke()

        assertTrue(result.isFailure)
        assertTrue(!clearPin.called)
        assertEquals(AppLockTypePreference.Pin, repo.getAppLockTypePreference().first())
        assertEquals(AppLockState.Enabled, repo.getAppLockState().first())
    }
}
