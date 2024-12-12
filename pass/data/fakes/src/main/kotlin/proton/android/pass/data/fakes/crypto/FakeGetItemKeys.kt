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

package proton.android.pass.data.fakes.crypto

import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.data.api.crypto.GetItemKeys
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.ItemKey
import proton.android.pass.domain.key.ShareKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeGetItemKeys @Inject constructor() : GetItemKeys {

    private var itemKeys: Pair<ShareKey, ItemKey>? = null

    fun setItemKeys(itemKeys: Pair<ShareKey, ItemKey>) {
        this.itemKeys = itemKeys
    }

    override suspend fun invoke(
        userAddress: UserAddress,
        shareId: ShareId,
        itemId: ItemId
    ): Pair<ShareKey, ItemKey> = itemKeys ?: throw IllegalStateException("Item keys not set")

}
