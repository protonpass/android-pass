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

package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.ProtonFeatureFlagEntity
import javax.inject.Inject

interface LocalFeatureFlagDataSource {
    fun observeFeatureFlag(userId: UserId, featureName: String): Flow<Option<ProtonFeatureFlagEntity>>
    fun observeAllFeatureFlags(userId: UserId): Flow<List<ProtonFeatureFlagEntity>>
    suspend fun deleteFeatureFlags(userId: UserId, featureNames: List<String>)
    suspend fun storeFeatureFlags(featureFlags: List<ProtonFeatureFlagEntity>)
}

class LocalFeatureFlagDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalFeatureFlagDataSource {
    override fun observeFeatureFlag(userId: UserId, featureName: String): Flow<Option<ProtonFeatureFlagEntity>> =
        database.featureFlagsDao().observeFeatureFlag(userId.id, featureName).map { it.toOption() }

    override fun observeAllFeatureFlags(userId: UserId): Flow<List<ProtonFeatureFlagEntity>> =
        database.featureFlagsDao().observeAllForUser(userId.id)

    override suspend fun deleteFeatureFlags(userId: UserId, featureNames: List<String>) {
        database.inTransaction {
            featureNames.forEach { featureName ->
                database.featureFlagsDao().deleteFlag(userId.id, featureName)
            }
        }
    }

    override suspend fun storeFeatureFlags(featureFlags: List<ProtonFeatureFlagEntity>) {
        database.featureFlagsDao().insertOrUpdate(*featureFlags.toTypedArray())
    }
}
