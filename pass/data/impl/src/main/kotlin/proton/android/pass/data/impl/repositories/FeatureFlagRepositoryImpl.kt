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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.repositories.FeatureFlagRepository
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.ProtonFeatureFlagEntity
import proton.android.pass.data.impl.local.LocalFeatureFlagDataSource
import proton.android.pass.data.impl.remote.RemoteFeatureFlagDataSource
import proton.android.pass.data.impl.responses.FeatureFlagToggle
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class FeatureFlagRepositoryImpl @Inject constructor(
    private val local: LocalFeatureFlagDataSource,
    private val remote: RemoteFeatureFlagDataSource,
    private val accountManager: AccountManager,
    private val clock: Clock,
    private val database: PassDatabase
) : FeatureFlagRepository {

    override fun isFeatureEnabled(featureName: String, refresh: Boolean): Flow<Boolean> = flow {
        if (refresh) refresh()

        val userId = requireNotNull(accountManager.getPrimaryUserId().first())
        emit(userId)
    }.flatMapLatest { userId ->
        local.observeFeatureFlag(userId, featureName).map { it.isNotEmpty() }
    }

    override fun getFeatureValue(featureName: String, refresh: Boolean): Flow<Option<String>> =
        flow {
            if (refresh) refresh()

            val userId = requireNotNull(accountManager.getPrimaryUserId().first())
            emit(userId)
        }.flatMapLatest { userId ->
            local.observeFeatureFlag(userId, featureName)
                .map { entity ->
                    entity.map { it.variant }
                }
        }

    override suspend fun refresh(userId: UserId?) {
        val id = userId ?: requireNotNull(accountManager.getPrimaryUserId().first())

        PassLogger.d(TAG, "Fetching feature flags")
        val remoteFeatureFlags = remote.getFeatureFlags(id)
        val localFeatureFlags = local.observeAllFeatureFlags(id).first()

        // Pick the LocalFeatureFlags that are not in the remote response
        val toDelete = localFeatureFlags.filter { localFlag ->
            remoteFeatureFlags.none { remoteFlag -> remoteFlag.name == localFlag.name }
        }.map { it.name }

        // Pick the RemoteFeatureFlags that are not in the local database
        val toInsert = remoteFeatureFlags.filter { remoteFlag ->
            localFeatureFlags.none { localFlag -> remoteFlag.name == localFlag.name }
        }.map { it.toEntity(id) }

        // Pick the RemoteFeatureFlags that are also in the local database
        val toUpdate = remoteFeatureFlags.mapNotNull { remoteFlag ->
            val localFlag = localFeatureFlags.firstOrNull { localFlag -> remoteFlag.name == localFlag.name }
            if (localFlag != null) {
                remoteFlag.toEntity(id, createTime = localFlag.createTime)
            } else {
                null
            }
        }

        database.inTransaction {
            local.deleteFeatureFlags(id, toDelete)
            local.storeFeatureFlags(toInsert)
            local.storeFeatureFlags(toUpdate)
        }
    }

    private fun FeatureFlagToggle.toEntity(
        userId: UserId,
        createTime: Long? = null
    ): ProtonFeatureFlagEntity {
        val encodedVariant = Json.encodeToString(variant)
        val now = clock.now().epochSeconds
        val createdAt = createTime ?: now
        return ProtonFeatureFlagEntity(
            name = name,
            variant = encodedVariant,
            userId = userId.id,
            createTime = createdAt,
            updateTime = now
        )
    }

    companion object {
        private const val TAG = "FeatureFlagRepositoryImpl"
    }
}
