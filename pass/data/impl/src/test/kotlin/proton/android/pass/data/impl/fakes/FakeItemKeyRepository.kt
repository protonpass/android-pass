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

package proton.android.pass.data.impl.fakes

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.impl.repositories.ItemKeyRepository
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.key.ItemKey
import proton.android.pass.domain.key.ShareKey

class FakeItemKeyRepository : ItemKeyRepository {

    private var getLatestItemKeyFlow = testFlow<Pair<ShareKey, ItemKey>>()

    fun emitGetLatestItemKey(value: Pair<ShareKey, ItemKey>) {
        getLatestItemKeyFlow.tryEmit(value)
    }

    override fun getLatestItemKey(
        userId: UserId,
        addressId: AddressId,
        shareId: ShareId,
        groupEmail: String?,
        itemId: ItemId
    ): Flow<Pair<ShareKey, ItemKey>> = getLatestItemKeyFlow
}
