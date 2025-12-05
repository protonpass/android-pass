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

import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.usecases.CreateItem
import proton.android.pass.crypto.api.usecases.CreateItemPayload
import proton.android.pass.crypto.api.usecases.EncryptedCreateItem
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.key.ShareKey

class FakeCreateItem : CreateItem {

    private var payload: CreateItemPayload? = null

    fun setPayload(value: CreateItemPayload) {
        payload = value
    }

    override fun create(shareKey: ShareKey, itemContents: ItemContents): CreateItemPayload =
        payload ?: throw IllegalStateException("payload is not set")

    companion object {
        fun createPayload(): CreateItemPayload {
            val key = EncryptionKey.generate()
            return CreateItemPayload(
                request = EncryptedCreateItem(
                    contentFormatVersion = 1,
                    content = FakeEncryptionContext.encrypt("content"),
                    keyRotation = 1,
                    itemKey = Base64.encodeBase64String(FakeEncryptionContext.encrypt(key.value()).array)
                ),
                itemKey = key
            )
        }
    }
}
