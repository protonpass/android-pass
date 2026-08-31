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
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.data.api.usecases.ClearPin
import proton.android.pass.data.api.usecases.SetAppLockType
import proton.android.pass.data.fakes.repositories.FakeAssetLinkRepository
import proton.android.pass.data.impl.fakes.FakeLocalAppLockTypeDataSource
import proton.android.pass.data.impl.local.AppLockTypeRecord
import proton.android.pass.preferences.AppLockTimePreference
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.preferences.FakePreferenceRepository
import kotlin.test.assertTrue

class ResetAppToDefaultsImplTest {

    private class RecordingClearPin : ClearPin {
        var called = false
        override suspend fun invoke() { called = true }
    }

    private class FailingSetAppLockType : SetAppLockType {
        override suspend fun invoke(type: AppLockTypePreference): Result<Unit> =
            Result.failure(RuntimeException("boom"))
    }

    private class SucceedingSetAppLockType : SetAppLockType {
        override suspend fun invoke(type: AppLockTypePreference): Result<Unit> = Result.success(Unit)
    }

    private fun instance(
        setAppLockType: SetAppLockType,
        localAppLockTypeDataSource: FakeLocalAppLockTypeDataSource,
        clearPin: ClearPin
    ) = ResetAppToDefaultsImpl(
        preferencesRepository = FakePreferenceRepository(),
        internalSettingsRepository = FakeInternalSettingsRepository(),
        setAppLockType = setAppLockType,
        localAppLockTypeDataSource = localAppLockTypeDataSource,
        clearPin = clearPin,
        assetLinkRepository = FakeAssetLinkRepository(),
        appDispatchers = FakeAppDispatchers()
    )

    @Test
    fun `does not force-clear the durable store or pin when SetAppLockType succeeds`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply {
            record = AppLockTypeRecord.Valid(AppLockTypePreference.Pin, AppLockTimePreference.Immediately)
        }
        val clearPin = RecordingClearPin()

        instance(SucceedingSetAppLockType(), ds, clearPin).invoke()

        assertTrue(!clearPin.called)
        assertTrue(ds.stored.isEmpty())
    }

    @Test
    fun `force-clears the durable store and pin when SetAppLockType fails`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply {
            record = AppLockTypeRecord.Valid(AppLockTypePreference.Pin, AppLockTimePreference.Immediately)
        }
        val clearPin = RecordingClearPin()

        instance(FailingSetAppLockType(), ds, clearPin).invoke()

        assertTrue(clearPin.called)
        assertTrue(ds.read() is AppLockTypeRecord.Absent)
    }

    @Test
    fun `clears the pin even if the forced durable store drop also fails`() = runTest {
        val ds = FakeLocalAppLockTypeDataSource().apply {
            record = AppLockTypeRecord.Valid(AppLockTypePreference.Pin, AppLockTimePreference.Immediately)
            storeResult = Result.failure(RuntimeException("disk"))
        }
        val clearPin = RecordingClearPin()

        instance(FailingSetAppLockType(), ds, clearPin).invoke()

        assertTrue(clearPin.called)
    }
}
