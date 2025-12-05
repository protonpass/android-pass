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

package proton.android.pass.crypto.fakes.usecases

import proton.android.pass.crypto.api.usecases.EncryptedUpdateItemRequest
import proton.android.pass.crypto.api.usecases.UpdateItem
import proton.android.pass.domain.key.ItemKey
import proton_pass_item_v1.ItemV1

class FakeUpdateItem : UpdateItem {

    private var request: EncryptedUpdateItemRequest? = null

    fun setRequest(value: EncryptedUpdateItemRequest) {
        request = value
    }

    override fun createRequest(
        itemKey: ItemKey,
        itemContent: ItemV1.Item,
        lastRevision: Long
    ): EncryptedUpdateItemRequest = request ?: throw IllegalStateException("request is not set")
}
