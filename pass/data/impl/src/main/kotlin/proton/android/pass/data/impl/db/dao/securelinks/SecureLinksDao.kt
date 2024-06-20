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

package proton.android.pass.data.impl.db.dao.securelinks

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import proton.android.pass.data.impl.db.entities.securelinks.SecureLinkEntity

@Dao
abstract class SecureLinksDao : BaseDao<SecureLinkEntity>() {

    @Query(
        """
            SELECT * FROM ${SecureLinkEntity.TABLE_NAME}
            WHERE ${SecureLinkEntity.Columns.USER_ID} = :userId
            ORDER BY ${SecureLinkEntity.Columns.EXPIRATION} ASC
        """
    )
    abstract fun observeSecureLinks(userId: String): Flow<List<SecureLinkEntity>>

    @Query(
        """
            SELECT * FROM ${SecureLinkEntity.TABLE_NAME}
            WHERE ${SecureLinkEntity.Columns.USER_ID} = :userId
            AND ${SecureLinkEntity.Columns.LINK_ID} = :linkId
        """
    )
    abstract fun observeSecureLink(userId: String, linkId: String): Flow<SecureLinkEntity>

    @Query(
        """
            SELECT COUNT(${SecureLinkEntity.Columns.LINK_ID}) FROM ${SecureLinkEntity.TABLE_NAME}
            WHERE ${SecureLinkEntity.Columns.USER_ID} = :userId
        """
    )
    abstract fun observeSecureLinksCount(userId: String): Flow<Int>

}
