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

package proton.android.pass.initializer

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import proton.android.pass.biometry.AuthOverrideState
import proton.android.pass.biometry.ExtendAuthTime
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.commonui.api.PassAppLifecycleProvider
import proton.android.pass.commonui.api.PassAppLifecycleProvider.State
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.UserPreferencesRepository

class AppLockListenerInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val entryPoint: AppLockInitializerEntryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                AppLockInitializerEntryPoint::class.java
            )

        val lifecycleProvider = entryPoint.passAppLifecycleProvider()
        val needsBiometricAuth = entryPoint.needsBiometricAuth()
        val userPreferencesRepository = entryPoint.userPreferencesRepository()
        val extendAuthTime = entryPoint.extendAuthTime()
        lifecycleProvider.state
            .map {
                when (it) {
                    State.Foreground -> if (!needsBiometricAuth().first()) {
                        userPreferencesRepository.setHasAuthenticated(HasAuthenticated.Authenticated)
                        entryPoint.authOverrideState().setAuthOverride(false)
                    }
                    State.Background -> extendAuthTime()
                }
            }
            .flowWithLifecycle(lifecycleProvider.lifecycle, Lifecycle.State.CREATED)
            .launchIn(lifecycleProvider.lifecycle.coroutineScope)
    }

    override fun dependencies(): List<Class<out Initializer<*>?>> = emptyList()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppLockInitializerEntryPoint {
        fun passAppLifecycleProvider(): PassAppLifecycleProvider
        fun needsBiometricAuth(): NeedsBiometricAuth
        fun userPreferencesRepository(): UserPreferencesRepository
        fun extendAuthTime(): ExtendAuthTime
        fun authOverrideState(): AuthOverrideState
    }
}
