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

import kotlinx.coroutines.runBlocking
import proton.android.pass.common.api.some
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

class StoreAuthSuccessfulImpl @Inject constructor(
    private val biometryAuthTimeHolder: BiometryAuthTimeHolder,
    private val bootCountRetriever: BootCountRetriever,
    private val elapsedTimeProvider: ElapsedTimeProvider,
    private val preferencesRepository: UserPreferencesRepository,
    private val internalSettingsRepository: InternalSettingsRepository
) : StoreAuthSuccessful {
    override fun invoke() {
        runBlocking {
            preferencesRepository.setHasAuthenticated(HasAuthenticated.Authenticated)
        }
        internalSettingsRepository.setMasterPasswordAttemptsCount(0)
        internalSettingsRepository.setPinAttemptsCount(0)

        biometryAuthTimeHolder.storeBiometryAuthData(
            AuthData(
                authTime = elapsedTimeProvider.getElapsedTime().some(),
                bootCount = bootCountRetriever.get().some()
            )
        )
    }
}
