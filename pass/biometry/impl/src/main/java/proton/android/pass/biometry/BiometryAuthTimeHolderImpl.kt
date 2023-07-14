/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.biometry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import proton.android.pass.common.api.Some
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.InternalSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometryAuthTimeHolderImpl @Inject constructor(
    private val internalSettingsRepository: InternalSettingsRepository
) : BiometryAuthTimeHolder {

    override fun getBiometryAuthData(): Flow<AuthData> = combine(
        internalSettingsRepository.getLastUnlockedTime(),
        internalSettingsRepository.getBootCount(),
        ::AuthData
    )

    override fun storeBiometryAuthData(data: AuthData) {
        val authTime = data.authTime
        if (authTime is Some) {
            internalSettingsRepository.setLastUnlockedTime(authTime.value)
                .onFailure {
                    PassLogger.w(TAG, it, "Error storing last unlocked time")
                }
        }

        val bootCount = data.bootCount
        if (bootCount is Some)
            internalSettingsRepository.setBootCount(bootCount.value)
                .onFailure {
                    PassLogger.w(TAG, it, "Error storing boot count")
                }
    }

    companion object {
        private const val TAG = "BiometryAuthTimeHolderImpl"
    }
}
