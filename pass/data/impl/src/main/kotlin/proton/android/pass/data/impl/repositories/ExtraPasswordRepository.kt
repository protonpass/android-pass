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
import proton.android.pass.crypto.api.HashUtils
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import java.io.File
import javax.inject.Inject

interface ExtraPasswordRepository {
    suspend fun doesUserHaveExtraPassword(userId: UserId): Boolean
    suspend fun storeAccessKeyForUser(userId: UserId, extraPassword: EncryptedString)
    suspend fun checkAccessKeyForUser(userId: UserId, extraPassword: EncryptedString): Boolean
    suspend fun removeLocalExtraPasswordForUser(userId: UserId)
}

class ExtraPasswordRepositoryImpl @Inject constructor(
    private val appDispatchers: AppDispatchers,
    @param:ApplicationContext private val appContext: Context,
    private val encryptionContextProvider: EncryptionContextProvider
) : ExtraPasswordRepository {
    override suspend fun storeAccessKeyForUser(userId: UserId, extraPassword: EncryptedString) =
        withContext(appDispatchers.io) {
            val file = getExtraPasswordFileForUser(userId)
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()

            val encryptedHashedPassword = encryptionContextProvider.withEncryptionContext {
                val decryptedPassword = decrypt(extraPassword)
                val hashedPassword = HashUtils.sha256(decryptedPassword)
                encrypt(hashedPassword.encodeToByteArray())
            }
            file.writeBytes(encryptedHashedPassword.array)
        }

    override suspend fun checkAccessKeyForUser(userId: UserId, extraPassword: EncryptedString): Boolean =
        withContext(appDispatchers.io) {
            val file = getExtraPasswordFileForUser(userId)
            if (!file.exists()) {
                throw IllegalStateException(
                    "Trying to check access key for user but we don't have it user=${userId.id}"
                )
            }

            val contents = file.readBytes()
            val (hashedUserPassword, decryptedHashedStoredPassword) = encryptionContextProvider
                .withEncryptionContext {
                    val decryptedUserPassword = decrypt(extraPassword)

                    val decryptedHashedStoredPassword = EncryptedByteArray(contents)
                    val decryptedPasswordHash = decrypt(decryptedHashedStoredPassword)
                    val decryptedPasswordHashAsString = decryptedPasswordHash.decodeToString()

                    HashUtils.sha256(decryptedUserPassword) to decryptedPasswordHashAsString
                }

            return@withContext hashedUserPassword == decryptedHashedStoredPassword
        }

    override suspend fun removeLocalExtraPasswordForUser(userId: UserId) = withContext(appDispatchers.io) {
        val file = getExtraPasswordFileForUser(userId)
        if (file.exists()) {
            file.delete()
        }
    }

    override suspend fun doesUserHaveExtraPassword(userId: UserId): Boolean = withContext(appDispatchers.io) {
        getExtraPasswordFileForUser(userId).exists()
    }

    private fun getExtraPasswordFileForUser(userId: UserId): File =
        File(appContext.dataDir, getExtraPasswordFilenameForUser(userId))

    private fun getExtraPasswordFilenameForUser(userId: UserId) = "access_key_${userId.id}"
}
