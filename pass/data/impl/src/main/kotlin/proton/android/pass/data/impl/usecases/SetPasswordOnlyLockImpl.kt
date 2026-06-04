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
import proton.android.pass.data.api.usecases.ClearPin
import proton.android.pass.data.api.usecases.SetPasswordOnlyLock
import proton.android.pass.data.impl.local.LocalAppLockTypeDataSource
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SetPasswordOnlyLockImpl @Inject constructor(
    private val localAppLockTypeDataSource: LocalAppLockTypeDataSource,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clearPin: ClearPin
) : SetPasswordOnlyLock {

    override suspend fun invoke(): Result<Unit> {
        val time = userPreferencesRepository.getAppLockTimePreference().first()
        val previousType = userPreferencesRepository.getAppLockTypePreference().first()
        PassLogger.i(TAG, "Switching to password only lock (from $previousType, time=$time)")

        // Drop the durable record first: it is the source of truth, and while it still holds
        // Pin/Biometrics the next reconcile would restore that type over the None written below —
        // resurrecting a lock whose PIN this call is about to delete.
        val storeResult = localAppLockTypeDataSource.store(AppLockTypePreference.None, time)
        if (storeResult.isFailure) {
            storeResult.exceptionOrNull()
                ?.let { PassLogger.w(TAG, it, "Failed to drop durable app lock store; aborting") }
            return storeResult
        }

        clearPin()

        // The proto writes cannot be transactional, so order them such that no intermediate state
        // is both None and Disabled: an organisation that enforces a lock must never observe an
        // unlocked app, not even between these two writes.
        return runCatching {
            userPreferencesRepository.setAppLockState(AppLockState.Enabled).getOrThrow()
            userPreferencesRepository.setAppLockTypePreference(AppLockTypePreference.None).getOrThrow()
        }
            .onSuccess { PassLogger.i(TAG, "Password only lock set") }
            .onFailure { PassLogger.w(TAG, it, "Failed to set password only lock") }
    }

    private companion object {
        private const val TAG = "SetPasswordOnlyLockImpl"
    }
}
