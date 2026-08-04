/*
 * Copyright (c) 2024-2026 Proton AG
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

package proton.android.pass.account.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.featureflag.domain.entity.Scope
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeFeatureFlagRepository @Inject constructor() : FeatureFlagRepository {

    private val flagsFlow = MutableStateFlow<Map<FeatureId, FeatureFlag?>>(emptyMap())

    fun setFeatureFlag(featureId: FeatureId, featureFlag: FeatureFlag?) {
        flagsFlow.update { it + (featureId to featureFlag) }
    }

    override suspend fun get(
        userId: UserId?,
        featureIds: Set<FeatureId>,
        refresh: Boolean
    ): List<FeatureFlag> = flagsFlow.value.filterKeys { it in featureIds }.values.filterNotNull()

    override suspend fun get(
        userId: UserId?,
        featureId: FeatureId,
        refresh: Boolean
    ): FeatureFlag? = flagsFlow.value[featureId]

    override suspend fun getAll(userId: UserId?): List<FeatureFlag> = flagsFlow.value.values.filterNotNull()
    override suspend fun awaitNotEmptyScope(userId: UserId?, scope: Scope) = Unit

    override fun getValue(userId: UserId?, featureId: FeatureId): Boolean? = flagsFlow.value[featureId]?.value

    override fun observe(
        userId: UserId?,
        featureIds: Set<FeatureId>,
        refresh: Boolean
    ): Flow<List<FeatureFlag>> = flagsFlow.map { flags ->
        flags.filterKeys { it in featureIds }.values.filterNotNull()
    }

    override fun observe(
        userId: UserId?,
        featureId: FeatureId,
        refresh: Boolean
    ): Flow<FeatureFlag?> = flagsFlow.map { it[featureId] }

    override fun prefetch(userId: UserId?, featureIds: Set<FeatureId>) {

    }

    override fun refreshAllOneTime(userId: UserId?) {
    }

    override fun refreshAllPeriodic(userId: UserId?, immediately: Boolean) {
    }

    override suspend fun update(featureFlag: FeatureFlag) {
        setFeatureFlag(featureFlag.featureId, featureFlag)
    }
}
