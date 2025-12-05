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

package proton.android.pass.account.fakes

import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicAddressInfo
import me.proton.core.key.domain.entity.key.PublicAddressKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.key.domain.entity.key.Recipient
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.key.domain.repository.Source
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakePublicAddressRepository @Inject constructor() : PublicAddressRepository {

    private val addressList: MutableMap<String, PublicAddress> = mutableMapOf()

    fun setAddress(
        address: String,
        keys: List<PublicAddressKey> = emptyList(),
        recipientType: Int = Recipient.Internal.value
    ) {
        addressList[address] = PublicAddress(
            email = address,
            recipientType = recipientType,
            mimeType = null,
            keys = keys,
            signedKeyList = null,
            ignoreKT = null
        )
    }

    fun setAddressWithDefaultKey(address: String, recipientType: Int = Recipient.Internal.value) {
        val key = PublicKey(
            key = "InvitedKey",
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true
        )
        val publicAddressKey = PublicAddressKey(
            email = address,
            flags = 0,
            publicKey = key
        )
        setAddress(address, listOf(publicAddressKey), recipientType)
    }

    override suspend fun clearAll() {
        addressList.clear()
    }

    override suspend fun getPublicAddress(
        sessionUserId: SessionUserId,
        email: String,
        source: Source
    ): PublicAddress = addressList[email] ?: throw IllegalStateException("Address not set")

    override suspend fun getPublicAddressInfo(
        sessionUserId: SessionUserId,
        email: String,
        internalOnly: Boolean,
        source: Source
    ): PublicAddressInfo {
        throw IllegalStateException("This method should not be called")
    }


    override suspend fun getSKLAtEpoch(
        userId: UserId,
        epochId: Int,
        email: String
    ): PublicSignedKeyList {
        throw IllegalStateException("This method should not be called")
    }

    override suspend fun getSKLsAfterEpoch(
        userId: UserId,
        epochId: Int,
        email: String
    ): List<PublicSignedKeyList> {
        throw IllegalStateException("This method should not be called")
    }
}
