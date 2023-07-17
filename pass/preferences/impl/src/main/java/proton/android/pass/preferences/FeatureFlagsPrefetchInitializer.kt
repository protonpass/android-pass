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

package proton.android.pass.preferences

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.onAccountState
import proton.android.pass.commonui.api.PassAppLifecycleProvider
import proton.android.pass.data.api.repositories.FeatureFlagRepository

class FeatureFlagsPrefetchInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val entryPoint: FeatureFlagsPrefetchInitializerEntryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                FeatureFlagsPrefetchInitializerEntryPoint::class.java
            )
        val featureFlagManager: FeatureFlagRepository = entryPoint.featureFlagManager()
        val accountManager: AccountManager = entryPoint.accountManager()
        val passAppLifecycleProvider: PassAppLifecycleProvider =
            entryPoint.passAppLifecycleProvider()

        accountManager.onAccountState(AccountState.Ready, initialState = true)
            .flowWithLifecycle(passAppLifecycleProvider.lifecycle, Lifecycle.State.CREATED)
            .onEach { _ ->
                val featureFlags: Set<String> = FeatureFlag.values()
                    .mapNotNull { it.key }
                    .toSet()
                if (featureFlags.isNotEmpty()) {
                    featureFlagManager.refresh()
                }
            }
            .launchIn(passAppLifecycleProvider.lifecycle.coroutineScope)
    }

    override fun dependencies(): List<Class<out Initializer<*>?>> = emptyList()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FeatureFlagsPrefetchInitializerEntryPoint {
        fun passAppLifecycleProvider(): PassAppLifecycleProvider
        fun accountManager(): AccountManager
        fun featureFlagManager(): FeatureFlagRepository
    }
}
