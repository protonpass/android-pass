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
import androidx.lifecycle.coroutineScope
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.commonui.api.PassAppLifecycleProvider
import proton.android.pass.data.api.repositories.FeatureFlagRepository
import proton.android.pass.log.api.PassLogger

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
        passAppLifecycleProvider.lifecycle.coroutineScope.launch {
            combine(
                passAppLifecycleProvider.state,
                accountManager.getPrimaryUserId().flowOn(Dispatchers.IO)
            ) { state, userId -> state to userId }
                .catch { PassLogger.w(TAG, it) }
                .collectLatest { (state, userId) ->
                    if (state == PassAppLifecycleProvider.State.Foreground && userId != null) {
                        val featureFlags: Set<String> = FeatureFlag.values()
                            .mapNotNull { flag -> flag.key }
                            .toSet()

                        runCatching {
                            if (featureFlags.isNotEmpty()) {
                                withContext(Dispatchers.IO) {
                                    featureFlagManager.refresh(userId)
                                }
                            }
                        }
                            .onSuccess { PassLogger.i(TAG, "Feature flags refreshed") }
                            .onFailure {
                                PassLogger.w(TAG, "Failed to refresh feature flags")
                                PassLogger.w(TAG, it)
                            }
                    }
                }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>?>> = emptyList()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface FeatureFlagsPrefetchInitializerEntryPoint {
        fun passAppLifecycleProvider(): PassAppLifecycleProvider
        fun accountManager(): AccountManager
        fun featureFlagManager(): FeatureFlagRepository
    }

    companion object {
        private const val TAG = "FeatureFlagsPrefetchInitializer"
    }
}
