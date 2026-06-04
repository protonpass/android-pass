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
import proton.android.pass.data.api.usecases.ReconcileAppLock
import proton.android.pass.data.api.usecases.ReconcileAppLockResult
import proton.android.pass.data.impl.local.AppLockTypeRecord
import proton.android.pass.data.impl.local.LocalAppLockTypeDataSource
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.AppLockTimePreference
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReconcileAppLockImpl @Inject constructor(
    private val localAppLockTypeDataSource: LocalAppLockTypeDataSource,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val internalSettingsRepository: InternalSettingsRepository
) : ReconcileAppLock {

    override suspend fun invoke(): ReconcileAppLockResult = when (val record = localAppLockTypeDataSource.read()) {
        is AppLockTypeRecord.Valid -> {
            PassLogger.i(TAG, "Durable record present: type=${record.type}, time=${record.time}")
            if (alignProto(record.type, record.time)) {
                ReconcileAppLockResult.Ok
            } else {
                PassLogger.w(TAG, "Durable record could not be reflected into proto; requiring re-auth")
                ReconcileAppLockResult.RequireReauth
            }
        }

        AppLockTypeRecord.Absent -> {
            PassLogger.i(TAG, "Durable record absent")
            if (protoHoldsLock()) {
                PassLogger.i(TAG, "Proto holds a lock; durable store needs seeding")
                ReconcileAppLockResult.SeedRequired
            } else {
                ReconcileAppLockResult.Ok
            }
        }

        AppLockTypeRecord.Corrupted -> {
            PassLogger.w(TAG, "Durable record corrupted; requiring re-auth")
            // Drop the unusable file so the next launch doesn't re-trigger this.
            localAppLockTypeDataSource.store(AppLockTypePreference.None, AppLockTimePreference.Immediately)
                .onFailure { PassLogger.w(TAG, it, "Failed to drop corrupted app lock record") }
            ReconcileAppLockResult.RequireReauth
        }
    }

    private suspend fun alignProto(type: AppLockTypePreference, time: AppLockTimePreference): Boolean {
        val protoType = userPreferencesRepository.getAppLockTypePreference().first()
        val protoState = userPreferencesRepository.getAppLockState().first()
        val protoTime = userPreferencesRepository.getAppLockTimePreference().first()
        PassLogger.i(TAG, "Proto lock: type=$protoType, state=$protoState, time=$protoTime")
        var restoredLock = false
        var lockReflected = true
        if (protoType != type) {
            PassLogger.w(TAG, "Proto lock type lost ($protoType != $type); restoring from durable store")
            userPreferencesRepository.setAppLockTypePreference(type)
                .onFailure {
                    lockReflected = false
                    PassLogger.w(TAG, it, "Failed to restore lock type")
                }
                .onSuccess {
                    restoredLock = true
                    userPreferencesRepository.setAppLockState(AppLockState.Enabled)
                        .onFailure {
                            lockReflected = false
                            PassLogger.w(TAG, it, "Failed to restore lock state")
                        }
                }
        } else if (protoState is AppLockState.Disabled) {
            PassLogger.w(TAG, "Proto lock state lost while type matches; re-enabling")
            restoredLock = true
            userPreferencesRepository.setAppLockState(AppLockState.Enabled)
                .onFailure {
                    lockReflected = false
                    PassLogger.w(TAG, it, "Failed to re-enable lock state")
                }
        } else {
            PassLogger.i(TAG, "Proto lock already aligned with durable store")
        }

        if (protoTime != time) {
            PassLogger.w(TAG, "Proto lock time drifted ($protoTime != $time); restoring")
            userPreferencesRepository.setAppLockTimePreference(time)
                .onFailure { PassLogger.w(TAG, it, "Failed to restore lock time") }
        }

        return if (restoredLock) invalidateUnlockState() && lockReflected else lockReflected
    }

    private suspend fun invalidateUnlockState(): Boolean {
        PassLogger.w(TAG, "Lock state was restored; invalidating unlock window")
        var invalidated = true
        internalSettingsRepository.setLastUnlockedTime(0L)
            .onFailure {
                invalidated = false
                PassLogger.w(TAG, it, "Failed to invalidate unlock time")
            }
        userPreferencesRepository.setHasAuthenticated(HasAuthenticated.NotAuthenticated)
            .onFailure {
                invalidated = false
                PassLogger.w(TAG, it, "Failed to reset authenticated flag")
            }
        return invalidated
    }

    private suspend fun protoHoldsLock(): Boolean {
        val protoType = userPreferencesRepository.getAppLockTypePreference().first()
        val protoState = userPreferencesRepository.getAppLockState().first()
        return protoType != AppLockTypePreference.None && protoState is AppLockState.Enabled
    }

    private companion object {
        private const val TAG = "ReconcileAppLockImpl"
    }
}
