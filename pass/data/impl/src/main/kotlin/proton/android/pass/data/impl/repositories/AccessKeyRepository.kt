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

package proton.android.pass.data.impl.repositories

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import java.io.File
import javax.inject.Inject

interface AccessKeyRepository {
    suspend fun storeAccessKeyForUser(userId: UserId, accessKey: EncryptedString)
    suspend fun checkAccessKeyForUser(userId: UserId, accessKey: EncryptedString): Boolean
    suspend fun removeAccessKeyForUser(userId: UserId)
}

class AccessKeyRepositoryImpl @Inject constructor(
    private val appDispatchers: AppDispatchers,
    @ApplicationContext private val appContext: Context,
    private val encryptionContextProvider: EncryptionContextProvider
) : AccessKeyRepository {
    override suspend fun storeAccessKeyForUser(userId: UserId, accessKey: EncryptedString) =
        withContext(appDispatchers.io) {
            val file = getAccessKeyFileForUser(userId)
            if (file.exists()) {
                file.delete()
            }

            file.writeBytes(accessKey.encodeToByteArray())
        }

    override suspend fun checkAccessKeyForUser(userId: UserId, accessKey: EncryptedString): Boolean =
        withContext(appDispatchers.io) {
            val file = getAccessKeyFileForUser(userId)
            if (!file.exists()) {
                throw IllegalStateException(
                    "Trying to check access key for user but we don't have it user=${userId.id}"
                )
            }

            val contents = file.readBytes()
            val (decryptedAccessKey, decryptedContents) = encryptionContextProvider.withEncryptionContext {
                decrypt(accessKey) to decrypt(EncryptedByteArray(contents)).decodeToString()
            }

            return@withContext decryptedAccessKey == decryptedContents
        }

    override suspend fun removeAccessKeyForUser(userId: UserId) = withContext(appDispatchers.io) {
        val file = getAccessKeyFileForUser(userId)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun getAccessKeyFileForUser(userId: UserId): File =
        File(appContext.dataDir, getAccessKeyFilenameForUser(userId))

    private fun getAccessKeyFilenameForUser(userId: UserId) = "access_key_${userId.id}"
}
