/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.data.impl.local.securelinks

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.securelinks.SecureLink
import proton.android.pass.domain.securelinks.SecureLinkId

interface SecureLinksLocalDataSource {

    suspend fun create(userId: UserId, secureLink: SecureLink)

    suspend fun delete(userId: UserId, secureLinkId: SecureLinkId)

    suspend fun delete(userId: UserId, secureLinks: List<SecureLink>)

    suspend fun deleteAllInactive(userId: UserId)

    suspend fun getAll(userId: UserId): List<SecureLink>

    suspend fun getCount(userId: UserId): Int

    fun observe(userId: UserId, secureLinkId: SecureLinkId): Flow<SecureLink>

    fun observeAll(userId: UserId): Flow<List<SecureLink>>

    fun observeCount(userId: UserId): Flow<Int>

    suspend fun read(userId: UserId, secureLinkId: SecureLinkId): SecureLink

    suspend fun update(userId: UserId, secureLinks: List<SecureLink>)

}
