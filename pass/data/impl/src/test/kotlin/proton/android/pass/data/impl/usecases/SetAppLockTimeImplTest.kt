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
import proton.android.pass.data.impl.fakes.FakeLocalAppLockTypeDataSource
import proton.android.pass.preferences.AppLockTimePreference
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.preferences.UserPreferencesRepository
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SetAppLockTimeImplTest {

    private fun instance(ds: FakeLocalAppLockTypeDataSource, repo: UserPreferencesRepository) =
        SetAppLockTimeImpl(ds, repo)

    @Test
    fun `with a lock set, persists time to durable store and proto`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource()
        val repo = FakePreferenceRepository().apply {
            setAppLockTypePreference(AppLockTypePreference.Pin)
        }

        instance(ds, repo).invoke(AppLockTimePreference.Immediately)

        assertEquals(
            listOf(AppLockTypePreference.Pin to AppLockTimePreference.Immediately),
            ds.stored
        )
        assertEquals(AppLockTimePreference.Immediately, repo.getAppLockTimePreference().first())
    }

    @Test
    fun `with no lock, sets proto time without touching durable store`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource()
        val repo = FakePreferenceRepository().apply {
            setAppLockTypePreference(AppLockTypePreference.None)
        }

        instance(ds, repo).invoke(AppLockTimePreference.InFiveMinutes)

        assertTrue(ds.stored.isEmpty())
        assertEquals(AppLockTimePreference.InFiveMinutes, repo.getAppLockTimePreference().first())
    }

    @Test
    fun `aborts and does not set proto time when durable write fails`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply {
            storeResult = Result.failure(RuntimeException("disk"))
        }
        val repo = FakePreferenceRepository().apply {
            setAppLockTypePreference(AppLockTypePreference.Pin)
            setAppLockTimePreference(AppLockTimePreference.InTwoMinutes)
        }

        val result = instance(ds, repo).invoke(AppLockTimePreference.Immediately)

        assertTrue(result.isFailure)
        assertEquals(AppLockTimePreference.InTwoMinutes, repo.getAppLockTimePreference().first())
    }
}
