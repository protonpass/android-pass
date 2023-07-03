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
import kotlinx.coroutines.runBlocking
import proton.android.pass.common.api.Option
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.InternalSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometryAuthTimeHolderImpl @Inject constructor(
    private val internalSettingsRepository: InternalSettingsRepository
) : BiometryAuthTimeHolder {

    override fun getBiometryAuthTime(): Flow<Option<Long>> =
        internalSettingsRepository.getLastUnlockedTime()

    override fun storeBiometryAuthTime(instant: Long) {
        runBlocking {
            internalSettingsRepository.setLastUnlockedTime(instant)
                .onFailure {
                    PassLogger.w(TAG, it, "Error storing last unlocked time")
                }
        }
    }

    companion object {
        private const val TAG = "BiometryAuthTimeHolderImpl"
    }
}
