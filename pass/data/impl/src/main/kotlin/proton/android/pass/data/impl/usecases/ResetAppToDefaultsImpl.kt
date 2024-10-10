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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import proton.android.pass.data.api.repositories.AssetLinkRepository
import proton.android.pass.data.api.usecases.ClearPin
import proton.android.pass.data.api.usecases.ResetAppToDefaults
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

class ResetAppToDefaultsImpl @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val clearPin: ClearPin,
    private val assetLinkRepository: AssetLinkRepository
) : ResetAppToDefaults {
    override suspend fun invoke() {
        PassLogger.i(TAG, "Clearing preferences")
        preferencesRepository.clearPreferences()
            .onSuccess { PassLogger.i(TAG, "Preferences cleared") }
            .onFailure {
                PassLogger.w(TAG, "Error clearing preferences")
                PassLogger.w(TAG, it)
            }

        PassLogger.i(TAG, "Clearing internal settings")
        internalSettingsRepository.clearSettings()
            .onSuccess { PassLogger.d(TAG, "Internal settings cleared") }
            .onFailure {
                PassLogger.w(TAG, "Error clearing internal settings")
                PassLogger.w(TAG, it)
            }

        clearPin()
        runCatching { withContext(Dispatchers.IO) { assetLinkRepository.purgeAll() } }
            .onSuccess { PassLogger.d(TAG, "Asset links purged") }
            .onFailure {
                PassLogger.w(TAG, "Error purging asset links")
                PassLogger.w(TAG, it)
            }
    }

    companion object {
        private const val TAG = "ResetAppToDefaultsImpl"
    }
}
