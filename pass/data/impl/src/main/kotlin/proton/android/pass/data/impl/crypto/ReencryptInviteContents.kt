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

package proton.android.pass.data.impl.crypto

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.AcceptInvite
import proton.android.pass.crypto.api.usecases.EncryptedInviteKey
import proton.android.pass.crypto.api.usecases.InvitedUserMode
import proton.android.pass.data.impl.responses.PendingInviteResponse
import javax.inject.Inject

interface ReencryptInviteContents {
    suspend operator fun invoke(userId: UserId, invite: PendingInviteResponse): EncryptedByteArray
}

class ReencryptInviteContentsImpl @Inject constructor(
    private val acceptInvite: AcceptInvite,
    private val userRepository: UserRepository,
    private val userAddressRepository: UserAddressRepository,
    private val publicAddressRepository: PublicAddressRepository,
    private val encryptionContextProvider: EncryptionContextProvider
) : ReencryptInviteContents {
    override suspend fun invoke(userId: UserId, invite: PendingInviteResponse): EncryptedByteArray {
        val key = invite.keys.firstOrNull { it.keyRotation == invite.vaultData.contentKeyRotation }
            ?: throw IllegalStateException("No key found for invite")

        val user = userRepository.getUser(userId)
        val address = userAddressRepository.getAddresses(userId).primary()
            ?: throw IllegalStateException("No primary address found for user")
        val addressPrivateKeys = address.keys.map { it.privateKey }

        val inviterKeys = publicAddressRepository.getPublicAddress(userId, invite.inviterEmail)
            .keys
            .map { it.publicKey }

        val openKeys = acceptInvite(
            invitedUser = user,
            invitedUserAddressKeys = addressPrivateKeys,
            inviterAddressKeys = inviterKeys,
            keys = listOf(
                EncryptedInviteKey(
                    keyRotation = key.keyRotation,
                    key = key.key
                )
            ),
            invitedUserMode = InvitedUserMode.EXISTING_USER
        )
        val encryptedOpenKey = openKeys.keys.firstOrNull()
            ?: throw IllegalStateException("No open key found")

        val asEncryptionKey = encryptionContextProvider.withEncryptionContext {
            val decrypted = decrypt(encryptedOpenKey.localEncryptedKey)
            EncryptionKey(decrypted)
        }

        val decodedContent = Base64.decodeBase64(invite.vaultData.content)
        val decrypted = encryptionContextProvider.withEncryptionContext(asEncryptionKey) {
            decrypt(EncryptedByteArray(decodedContent), EncryptionTag.VaultContent)
        }

        val reencrypted = encryptionContextProvider.withEncryptionContext {
            encrypt(decrypted)
        }

        return reencrypted
    }
}
