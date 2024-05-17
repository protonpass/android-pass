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

import kotlinx.coroutines.flow.first
import proton.android.pass.common.api.some
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

class StoreAuthOnStopImpl @Inject constructor(
    private val biometryAuthTimeHolder: BiometryAuthTimeHolder,
    private val bootCountRetriever: BootCountRetriever,
    private val elapsedTimeProvider: ElapsedTimeProvider,
    private val userPreferencesRepository: UserPreferencesRepository
) : StoreAuthOnStop {
    override suspend fun invoke() {
        val isAuthenticated = userPreferencesRepository.getHasAuthenticated()
            .first() is HasAuthenticated.Authenticated
        if (isAuthenticated) {
            userPreferencesRepository.setHasAuthenticated(HasAuthenticated.NotAuthenticated)
            biometryAuthTimeHolder.storeBiometryAuthData(
                AuthData(
                    authTime = elapsedTimeProvider.getElapsedTime().some(),
                    bootCount = bootCountRetriever.get().some()
                )
            )
        }
    }
}
