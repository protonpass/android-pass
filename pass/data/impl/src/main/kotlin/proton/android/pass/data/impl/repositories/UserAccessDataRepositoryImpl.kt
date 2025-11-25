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
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.UserAccessDataRepository
import proton.android.pass.data.impl.db.entities.PlanEntity
import proton.android.pass.data.impl.db.entities.UserAccessDataEntity
import proton.android.pass.data.impl.local.LocalPlanDataSource
import proton.android.pass.data.impl.local.LocalUserAccessDataDataSource
import proton.android.pass.data.impl.remote.accessdata.RemoteUserAccessDataDataSource
import proton.android.pass.data.impl.responses.PlanResponse
import proton.android.pass.data.impl.responses.UserAccessResponse
import proton.android.pass.domain.UserAccessData
import javax.inject.Inject

class UserAccessDataRepositoryImpl @Inject constructor(
    private val remoteUserAccessDataDataSource: RemoteUserAccessDataDataSource,
    private val localUserAccessDataDataSource: LocalUserAccessDataDataSource,
    private val localPlanDataSource: LocalPlanDataSource,
    private val clock: Clock
) : UserAccessDataRepository {

    override fun observe(userId: UserId): Flow<UserAccessData?> = localUserAccessDataDataSource.observe(userId)
        .map { it?.toDomain() }

    override suspend fun update(userId: UserId, userAccessData: UserAccessData) {
        userAccessData.toEntity(userId)
            .also { userAccessDataEntity ->
                localUserAccessDataDataSource.store(userAccessDataEntity)
            }
    }

    override suspend fun refresh(userId: UserId) {
        val userAccessDataResponse = remoteUserAccessDataDataSource.retrieveUserAccessData(userId)
        val userAccessData = observe(userId).first()

        val response = when {
            userAccessData == null -> userAccessDataResponse
            userAccessData.isSimpleLoginSyncEnabled -> userAccessDataResponse
            else -> userAccessDataResponse.copy(
                accessResponse = userAccessDataResponse.accessResponse.copy(
                    userData = userAccessDataResponse.accessResponse.userData.copy(
                        pendingAliasToSync = userAccessData.simpleLoginSyncPendingAliasCount
                    )
                )
            )
        }
        val userAccessDataEntity = response.toEntity(userId)
        val planEntity = userAccessDataResponse.accessResponse.planResponse.toEntity(
            userId,
            clock.now().epochSeconds
        )
        localPlanDataSource.storePlan(planEntity)
        localUserAccessDataDataSource.store(userAccessDataEntity)
    }

    private fun PlanResponse.toEntity(userId: UserId, epochSeconds: Long): PlanEntity = PlanEntity(
        userId = userId.id,
        vaultLimit = vaultLimit ?: -1,
        aliasLimit = aliasLimit ?: -1,
        totpLimit = totpLimit ?: -1,
        type = type,
        internalName = internalName,
        displayName = displayName,
        hideUpgrade = hideUpgrade,
        trialEnd = trialEnd,
        updatedAt = epochSeconds
    )

    private fun UserAccessResponse.toEntity(userId: UserId) = UserAccessDataEntity(
        userId = userId.id,
        pendingInvites = accessResponse.pendingInvites,
        waitingNewUserInvites = accessResponse.waitingNewUserInvites,
        minVersionUpgrade = accessResponse.minVersionUpgrade,
        protonMonitorEnabled = accessResponse.monitorResponse.protonMonitorEnabled,
        aliasMonitorEnabled = accessResponse.monitorResponse.aliasMonitorEnabled,
        isSimpleLoginSyncEnabled = accessResponse.userData.isAliasSyncEnabled,
        simpleLoginSyncDefaultShareId = accessResponse.userData.defaultShareID.orEmpty(),
        simpleLoginSyncPendingAliasCount = accessResponse.userData.pendingAliasToSync,
        canManageSimpleLoginAliases = accessResponse.planResponse.manageAlias,
        storageAllowed = accessResponse.planResponse.storageAllowed,
        storageUsed = accessResponse.planResponse.storageUsed,
        storageQuota = accessResponse.planResponse.storageQuota,
        storageMaxFileSize = accessResponse.planResponse.storageMaxFileSize
    )

    private fun UserAccessDataEntity.toDomain() = UserAccessData(
        pendingInvites = pendingInvites,
        waitingNewUserInvites = waitingNewUserInvites,
        needsUpdate = minVersionUpgrade != null,
        protonMonitorEnabled = protonMonitorEnabled,
        aliasMonitorEnabled = aliasMonitorEnabled,
        minVersionUpgrade = minVersionUpgrade,
        isSimpleLoginSyncEnabled = isSimpleLoginSyncEnabled,
        simpleLoginSyncDefaultShareId = simpleLoginSyncDefaultShareId,
        simpleLoginSyncPendingAliasCount = simpleLoginSyncPendingAliasCount,
        canManageSimpleLoginAliases = canManageSimpleLoginAliases,
        storageAllowed = storageAllowed,
        storageUsed = storageUsed,
        storageQuota = storageQuota,
        storageMaxFileSize = storageMaxFileSize
    )

    private fun UserAccessData.toEntity(userId: UserId) = UserAccessDataEntity(
        userId = userId.id,
        pendingInvites = pendingInvites,
        waitingNewUserInvites = waitingNewUserInvites,
        minVersionUpgrade = minVersionUpgrade,
        protonMonitorEnabled = protonMonitorEnabled,
        aliasMonitorEnabled = aliasMonitorEnabled,
        isSimpleLoginSyncEnabled = isSimpleLoginSyncEnabled,
        simpleLoginSyncDefaultShareId = simpleLoginSyncDefaultShareId,
        simpleLoginSyncPendingAliasCount = simpleLoginSyncPendingAliasCount,
        canManageSimpleLoginAliases = canManageSimpleLoginAliases,
        storageAllowed = storageAllowed,
        storageUsed = storageUsed,
        storageQuota = storageQuota,
        storageMaxFileSize = storageMaxFileSize
    )
}
