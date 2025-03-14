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
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.UserAccessDataRepository
import proton.android.pass.data.impl.db.entities.UserAccessDataEntity
import proton.android.pass.data.impl.local.LocalUserAccessDataDataSource
import proton.android.pass.data.impl.remote.accessdata.RemoteUserAccessDataDataSource
import proton.android.pass.data.impl.responses.UserAccessResponse
import proton.android.pass.domain.UserAccessData
import javax.inject.Inject

class UserAccessDataRepositoryImpl @Inject constructor(
    private val remoteUserAccessDataDataSource: RemoteUserAccessDataDataSource,
    private val localUserAccessDataDataSource: LocalUserAccessDataDataSource
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
        val userAccessDataResponse = remoteUserAccessDataDataSource.getUserAccessData(userId)
        val userAccessData = observe(userId).first()

        when {
            userAccessData == null -> userAccessDataResponse
            userAccessData.isSimpleLoginSyncEnabled -> userAccessDataResponse
            else -> userAccessDataResponse.copy(
                accessResponse = userAccessDataResponse.accessResponse.copy(
                    userData = userAccessDataResponse.accessResponse.userData.copy(
                        pendingAliasToSync = userAccessData.simpleLoginSyncPendingAliasCount
                    )
                )
            )
        }.also { response -> localUserAccessDataDataSource.store(response.toEntity(userId)) }
    }

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
