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

package proton.android.pass.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    private val needsBiometricAuth: NeedsBiometricAuth,
    private val preferenceRepository: UserPreferencesRepository
) : ViewModel() {

    fun getRoute(): RootNavigation {
        return runBlocking {
            val needsAuth = needsBiometricAuth().first()
            if (needsAuth) {
                return@runBlocking RootNavigation.Auth
            }
            val hasCompletedOnBoarding = preferenceRepository.getHasCompletedOnBoarding().first()
            if (!hasCompletedOnBoarding.value()) {
                return@runBlocking RootNavigation.OnBoarding
            }
            RootNavigation.Home
        }
    }
}
