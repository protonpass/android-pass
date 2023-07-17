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

package proton.android.pass.data.impl.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.data.impl.db.entities.ProtonFeatureFlagEntity
import proton.android.pass.data.impl.local.LocalFeatureFlagDataSource

class TestLocalFeatureFlagDataSource : LocalFeatureFlagDataSource {

    private val observeFeatureFlagFlow: MutableStateFlow<Option<ProtonFeatureFlagEntity>> =
        MutableStateFlow(None)
    private val observeAllFeatureFlagsFlow: MutableStateFlow<List<ProtonFeatureFlagEntity>> =
        MutableStateFlow(emptyList())

    private val deleteMemory: MutableList<List<String>> = mutableListOf()
    private val storeMemory: MutableList<List<ProtonFeatureFlagEntity>> = mutableListOf()

    fun getDeleteMemory(): List<List<String>> = deleteMemory
    fun getStoreMemory(): List<List<ProtonFeatureFlagEntity>> = storeMemory

    fun emitFeatureFlag(value: Option<ProtonFeatureFlagEntity>) {
        observeFeatureFlagFlow.tryEmit(value)
    }

    fun emitAllFeatureFlags(value: List<ProtonFeatureFlagEntity>) {
        observeAllFeatureFlagsFlow.tryEmit(value)
    }

    override fun observeFeatureFlag(
        userId: UserId,
        featureName: String
    ): Flow<Option<ProtonFeatureFlagEntity>> = observeFeatureFlagFlow

    override fun observeAllFeatureFlags(userId: UserId): Flow<List<ProtonFeatureFlagEntity>> =
        observeAllFeatureFlagsFlow

    override suspend fun deleteFeatureFlags(userId: UserId, featureNames: List<String>) {
        deleteMemory.add(featureNames)
    }

    override suspend fun storeFeatureFlags(featureFlags: List<ProtonFeatureFlagEntity>) {
        storeMemory.add(featureFlags)
    }
}
