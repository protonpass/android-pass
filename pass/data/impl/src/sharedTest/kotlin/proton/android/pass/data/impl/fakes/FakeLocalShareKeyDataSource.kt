/*
 * Copyright (c) 2026 Proton AG
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
import kotlinx.coroutines.flow.flowOf
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.data.impl.db.entities.ShareKeyEntity
import proton.android.pass.data.impl.fakes.mother.ShareKeyEntityTestFactory
import proton.android.pass.data.impl.local.LocalShareKeyDataSource
import proton.android.pass.domain.ShareId

class FakeLocalShareKeyDataSource : LocalShareKeyDataSource {
    private val shareKeyByCompositeKey = mutableMapOf<Triple<String, String, Long>, ShareKeyEntity>()

    fun setShareKey(userId: UserId, shareId: ShareId, rotation: Long, keyBytes: ByteArray) {
        shareKeyByCompositeKey[Triple(userId.id, shareId.id, rotation)] = ShareKeyEntityTestFactory.create(
            rotation = rotation,
            userId = userId.id,
            shareId = shareId.id,
            symmetricallyEncryptedKey = FakeEncryptionContext.encrypt(keyBytes),
        )
    }

    override fun getAllShareKeysForShare(userId: UserId, shareId: ShareId): Flow<List<ShareKeyEntity>> =
        flowOf(
            shareKeyByCompositeKey
                .filter { it.key.first == userId.id && it.key.second == shareId.id }
                .values
                .toList()
        )

    override fun getForShareAndRotation(userId: UserId, shareId: ShareId, rotation: Long): Flow<ShareKeyEntity?> =
        flowOf(shareKeyByCompositeKey[Triple(userId.id, shareId.id, rotation)])

    override fun getLatestKeyForShare(shareId: ShareId): Flow<ShareKeyEntity> {
        error("Not needed in tests")
    }

    override suspend fun storeShareKeys(entities: List<ShareKeyEntity>) {
        entities.forEach {
            shareKeyByCompositeKey[Triple(it.userId, it.shareId, it.rotation)] = it
        }
    }
}
