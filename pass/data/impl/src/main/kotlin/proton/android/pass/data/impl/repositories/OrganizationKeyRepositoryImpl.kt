/*
 * Copyright (c) 2025 Proton AG
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

import kotlinx.coroutines.flow.MutableStateFlow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.remote.RemoteOrganizationKeyDataSource
import proton.android.pass.data.impl.responses.OrganizationKeyApiModel
import proton.android.pass.domain.OrganizationKey
import proton.android.pass.domain.repositories.OrganizationKeyRepository
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class OrganizationKeyRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteOrganizationKeyDataSource
) : OrganizationKeyRepository {

    private val organizationKeyCache = MutableStateFlow<Map<UserId, OrganizationKey>>(emptyMap())

    override suspend fun getOrganizationKey(userId: UserId, forceRefresh: Boolean): OrganizationKey? {
        if (!forceRefresh) {
            organizationKeyCache.value[userId]?.let { return it }
        }

        val organizationKey = remoteDataSource.retrieveOrganizationKey(userId)?.toDomain()

        return when {
            organizationKey == null -> null
            organizationKey.hasInvalidFields() -> {
                PassLogger.i(TAG, "Invalid organization key - missing required fields")
                null
            }
            else -> {
                organizationKeyCache.value = organizationKeyCache.value + (userId to organizationKey)
                organizationKey
            }
        }
    }

    private fun OrganizationKeyApiModel.toDomain(): OrganizationKey = OrganizationKey(
        privateKey = privateKey,
        token = token,
        signature = signature,
        passwordless = passwordless
    )

    companion object {
        private const val TAG = "OrganizationKeyRepositoryImpl"
    }
}
