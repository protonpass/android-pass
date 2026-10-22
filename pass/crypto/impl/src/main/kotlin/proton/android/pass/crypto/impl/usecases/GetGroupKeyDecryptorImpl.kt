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

package proton.android.pass.crypto.impl.usecases

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.user.domain.repository.UserAddressRepository
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.crypto.api.usecases.GetGroupKeyDecryptor
import proton.android.pass.crypto.api.usecases.GroupKeyDecryptor
import proton.android.pass.crypto.impl.CoreGroupKeyDecryptor
import proton.android.pass.domain.AddressId
import proton.android.pass.domain.UserId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

private const val TAG = "GetGroupKeyDecryptorImpl"

class GetGroupKeyDecryptorImpl @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val userAddressRepository: UserAddressRepository,
    private val publicAddressRepository: PublicAddressRepository
) : GetGroupKeyDecryptor {

    override suspend fun invoke(
        userId: UserId,
        addressId: AddressId,
        groupEmail: String
    ): GroupKeyDecryptor {
        val invitedAddress = userAddressRepository.getAddress(userId, addressId)
            ?: run {
                PassLogger.w(TAG, "Invited address ${addressId.id} not found locally for user ${userId.id}")
                throw IllegalStateException("Invited address not found")
            }
        val groupPublicKeys = safeRunCatching {
            publicAddressRepository.getPublicAddressInfo(
                sessionUserId = userId,
                email = groupEmail,
                internalOnly = false
            )
        }.onFailure { e ->
            PassLogger.w(TAG, "Failed to fetch public keys for group")
            PassLogger.w(TAG, e)
        }.getOrThrow()
        return CoreGroupKeyDecryptor(
            invitedAddress = invitedAddress,
            groupPublicKeys = PublicKeyRing(keys = groupPublicKeys.address.keys.map { it.publicKey }),
            cryptoContext = cryptoContext
        )
    }
}
