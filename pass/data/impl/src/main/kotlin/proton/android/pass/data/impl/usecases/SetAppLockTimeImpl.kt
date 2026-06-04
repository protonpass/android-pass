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
import proton.android.pass.data.api.usecases.SetAppLockTime
import proton.android.pass.data.impl.local.LocalAppLockTypeDataSource
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.AppLockTimePreference
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SetAppLockTimeImpl @Inject constructor(
    private val localAppLockTypeDataSource: LocalAppLockTypeDataSource,
    private val userPreferencesRepository: UserPreferencesRepository
) : SetAppLockTime {

    override suspend fun invoke(time: AppLockTimePreference): Result<Unit> {
        val type = userPreferencesRepository.getAppLockTypePreference().first()
        if (type != AppLockTypePreference.None) {
            val storeResult = localAppLockTypeDataSource.store(type, time)
            if (storeResult.isFailure) {
                storeResult.exceptionOrNull()
                    ?.let { PassLogger.w(TAG, it, "Failed to persist app lock time; aborting") }
                return storeResult
            }
        }
        return userPreferencesRepository.setAppLockTimePreference(time)
            .onFailure { PassLogger.w(TAG, it, "Failed to set app lock time") }
    }

    private companion object {
        private const val TAG = "SetAppLockTimeImpl"
    }
}
