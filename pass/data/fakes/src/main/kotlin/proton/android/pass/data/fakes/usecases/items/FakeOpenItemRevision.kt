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

package proton.android.pass.data.fakes.usecases.items

import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.data.api.usecases.items.OpenItemRevision
import proton.android.pass.domain.Item
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class FakeOpenItemRevision @Inject constructor() : OpenItemRevision {

    private var item: Item? = null

    fun setItem(newItem: Item) {
        item = newItem
    }

    override suspend fun invoke(shareId: ShareId, itemRevision: ItemRevision): Item = item
        ?: throw IllegalStateException(
            "Item cannot be null, did you forget to call setItem() before executing the use case?"
        )

}
