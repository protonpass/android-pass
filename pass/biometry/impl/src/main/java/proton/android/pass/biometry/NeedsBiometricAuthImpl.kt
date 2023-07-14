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
import kotlinx.coroutines.flow.distinctUntilChanged
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

class NeedsBiometricAuthImpl @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val authTimeHolder: BiometryAuthTimeHolder,
    private val bootCountRetriever: BootCountRetriever,
    private val elapsedTimeProvider: ElapsedTimeProvider
) : NeedsBiometricAuth {

    override fun invoke(): Flow<Boolean> = combine(
        preferencesRepository.getAppLockState().distinctUntilChanged(),
        preferencesRepository.getHasAuthenticated().distinctUntilChanged(),
        preferencesRepository.getAppLockTimePreference().distinctUntilChanged(),
        authTimeHolder.getBiometryAuthData().distinctUntilChanged()
    ) { biometricLock, hasAuthenticated, appLockTimePreference, biometryAuthTime ->
        NeedsAuthChecker.needsAuth(
            biometricLock = biometricLock,
            hasAuthenticated = hasAuthenticated,
            appLockTimePreference = appLockTimePreference,
            lastUnlockTime = biometryAuthTime.authTime,
            now = elapsedTimeProvider.getElapsedTime(),
            lastBootCount = biometryAuthTime.bootCount,
            bootCount = bootCountRetriever.get()
        ).value()
    }
}
