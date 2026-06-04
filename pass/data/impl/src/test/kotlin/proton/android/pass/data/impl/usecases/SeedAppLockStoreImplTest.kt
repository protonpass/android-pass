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

import kotlinx.coroutines.test.runTest
import org.junit.Test
import proton.android.pass.data.impl.fakes.FakeLocalAppLockTypeDataSource
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.AppLockTimePreference
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.FakePreferenceRepository
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeedAppLockStoreImplTest {

    @Test
    fun `seeds the durable store from an intact proto`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource()
        val repo = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Enabled)
            setAppLockTypePreference(AppLockTypePreference.Pin)
            setAppLockTimePreference(AppLockTimePreference.InFiveMinutes)
        }

        val result = SeedAppLockStoreImpl(ds, repo).invoke()

        assertTrue(result.isSuccess)
        assertEquals(listOf(AppLockTypePreference.Pin to AppLockTimePreference.InFiveMinutes), ds.stored)
    }

    @Test
    fun `does not seed when the proto no longer holds a lock`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource()
        val repo = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Enabled)
            setAppLockTypePreference(AppLockTypePreference.None)
        }

        val result = SeedAppLockStoreImpl(ds, repo).invoke()

        assertTrue(result.isSuccess)
        assertTrue(ds.stored.isEmpty())
    }

    @Test
    fun `does not seed when the lock is disabled`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource()
        val repo = FakePreferenceRepository().apply {
            setAppLockState(AppLockState.Disabled)
            setAppLockTypePreference(AppLockTypePreference.Biometrics)
        }

        val result = SeedAppLockStoreImpl(ds, repo).invoke()

        assertTrue(result.isSuccess)
        assertTrue(ds.stored.isEmpty())
    }
}
