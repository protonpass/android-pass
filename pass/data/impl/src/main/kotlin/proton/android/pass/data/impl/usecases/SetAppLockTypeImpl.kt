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
import proton.android.pass.data.api.usecases.SetAppLockType
import proton.android.pass.data.impl.local.LocalAppLockTypeDataSource
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.AppLockTimePreference
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SetAppLockTypeImpl @Inject constructor(
    private val localAppLockTypeDataSource: LocalAppLockTypeDataSource,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val clearPin: ClearPin
) : SetAppLockType {

    override suspend fun invoke(type: AppLockTypePreference): Result<Unit> {
        val time = userPreferencesRepository.getAppLockTimePreference().first()
        val previousType = userPreferencesRepository.getAppLockTypePreference().first()
        PassLogger.i(TAG, "Setting app lock type to $type (from $previousType, time=$time)")
        return if (type == AppLockTypePreference.None) {
            disableLock(time)
        } else {
            enableLock(type, time)
        }
    }

    private suspend fun enableLock(type: AppLockTypePreference, time: AppLockTimePreference): Result<Unit> {
        val storeResult = localAppLockTypeDataSource.store(type, time)
        if (storeResult.isFailure) {
            // The durable store is the source of truth — if it could not be written, do NOT
            // touch the proto or the PIN file, otherwise they would diverge from it and the
            // next reconcile would resurrect/lock based on a stale durable value.
            storeResult.exceptionOrNull()
                ?.let { PassLogger.w(TAG, it, "Failed to persist app lock type; aborting") }
            return storeResult
        }

        if (type != AppLockTypePreference.Pin) clearPin()

        return userPreferencesRepository.setAppLockTypePreference(type)
            .onFailure { PassLogger.w(TAG, it, "Failed to set app lock type") }
            .onSuccess {
                userPreferencesRepository.setAppLockState(AppLockState.Enabled)
                    .onFailure { PassLogger.w(TAG, it, "Failed to set app lock state") }
                PassLogger.i(TAG, "App lock enabled with type $type")
            }
    }

    private suspend fun disableLock(time: AppLockTimePreference): Result<Unit> {
        // Drop the durable store first, then disable the proto, then clear the PIN. This never
        // leaves the proto Disabled while the durable store still says Valid — which would unlock
        // the live session despite the durable source of truth still being locked — and never
        // clears the PIN while a PIN lock could still be resurrected. A partial failure leaves the
        // session locked (fail closed) with the PIN intact; the caller gets the failure and retries.
        val storeResult = localAppLockTypeDataSource.store(AppLockTypePreference.None, time)
        if (storeResult.isFailure) {
            storeResult.exceptionOrNull()
                ?.let { PassLogger.w(TAG, it, "Failed to delete durable app lock store; aborting") }
            return storeResult
        }

        return runCatching {
            userPreferencesRepository.setAppLockTypePreference(AppLockTypePreference.None).getOrThrow()
            userPreferencesRepository.setAppLockState(AppLockState.Disabled).getOrThrow()
            clearPin()
        }
            .onSuccess { PassLogger.i(TAG, "App lock disabled") }
            .onFailure { PassLogger.w(TAG, it, "Failed to disable app lock") }
    }

    private companion object {
        private const val TAG = "SetAppLockTypeImpl"
    }
}
