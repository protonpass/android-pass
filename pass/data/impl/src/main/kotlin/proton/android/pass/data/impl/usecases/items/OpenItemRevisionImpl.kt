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

package proton.android.pass.data.impl.usecases.items

import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.usecases.OpenItem
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.items.OpenItemRevision
import proton.android.pass.data.impl.extensions.toCrypto
import proton.android.pass.data.impl.repositories.ShareKeyRepository
import proton.android.pass.domain.Item
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class OpenItemRevisionImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository,
    private val shareKeyRepository: ShareKeyRepository,
    private val openItem: OpenItem
) : OpenItemRevision {

    override suspend fun invoke(
        shareId: ShareId,
        itemRevision: ItemRevision,
        encryptionContext: EncryptionContext
    ): Item {
        val userId = requireNotNull(accountManager.getPrimaryUserId().first())
        val addressId = shareRepository.getAddressForShareId(userId, shareId).addressId
        val share = shareRepository.getById(userId, shareId)
        val shareKeys = shareKeyRepository.getShareKeys(
            userId = userId,
            addressId = addressId,
            shareId = shareId,
            groupEmail = share.groupEmail
        ).first()

        return openItem.open(
            response = itemRevision.toCrypto(),
            share = share,
            shareKeys = shareKeys,
            encryptionContext = encryptionContext
        ).item
    }

}
