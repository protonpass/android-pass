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

package proton.android.pass.data.api.usecases

import proton.android.pass.data.api.repositories.PinItemsResult
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

interface PinItems {

    suspend operator fun invoke(shareId: ShareId, itemId: ItemId): Item {
        when (val result = invoke(listOf(shareId to itemId))) {
            is PinItemsResult.NonePinned -> throw result.exception
            is PinItemsResult.SomePinned -> {
                throw IllegalStateException("Cannot return SomePinned if there is only 1 item")
            }
            is PinItemsResult.AllPinned -> {
                return result.items.first()
            }
        }
    }

    suspend operator fun invoke(items: List<Pair<ShareId, ItemId>>): PinItemsResult
}
