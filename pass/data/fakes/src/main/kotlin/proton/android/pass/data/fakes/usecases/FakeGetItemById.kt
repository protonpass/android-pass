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

package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeGetItemById @Inject constructor() : GetItemById {

    private var fallbackResult: Result<Item> =
        Result.failure(IllegalStateException("Result not set"))
    private var storedResults: MutableMap<Pair<ShareId, ItemId>, Result<Item>> = mutableMapOf()

    fun emit(result: Result<Item>) {
        this.fallbackResult = result
    }

    fun emit(
        shareId: ShareId,
        itemId: ItemId,
        value: Result<Item>
    ) {
        storedResults[shareId to itemId] = value
    }

    override suspend fun invoke(
        userId: UserId?,
        shareId: ShareId,
        itemId: ItemId
    ): Item = storedResults[shareId to itemId]?.getOrThrow() ?: fallbackResult.getOrThrow()

}
