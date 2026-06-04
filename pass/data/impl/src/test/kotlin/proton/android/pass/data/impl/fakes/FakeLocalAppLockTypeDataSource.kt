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

package proton.android.pass.data.impl.fakes

import proton.android.pass.data.impl.local.AppLockTypeRecord
import proton.android.pass.data.impl.local.LocalAppLockTypeDataSource
import proton.android.pass.preferences.AppLockTimePreference
import proton.android.pass.preferences.AppLockTypePreference

class FakeLocalAppLockTypeDataSource : LocalAppLockTypeDataSource {
    var record: AppLockTypeRecord = AppLockTypeRecord.Absent
    var storeResult: Result<Unit> = Result.success(Unit)
    val stored = mutableListOf<Pair<AppLockTypePreference, AppLockTimePreference>>()

    override suspend fun store(type: AppLockTypePreference, time: AppLockTimePreference): Result<Unit> {
        if (storeResult.isFailure) return storeResult
        stored += type to time
        record = if (type == AppLockTypePreference.None) AppLockTypeRecord.Absent
        else AppLockTypeRecord.Valid(type, time)
        return storeResult
    }

    override suspend fun read(): AppLockTypeRecord = record
}
