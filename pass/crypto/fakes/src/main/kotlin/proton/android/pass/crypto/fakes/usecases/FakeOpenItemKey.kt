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

package proton.android.pass.crypto.fakes.usecases

import proton.android.pass.crypto.api.usecases.EncryptedItemKey
import proton.android.pass.crypto.api.usecases.OpenItemKey
import proton.android.pass.domain.key.InviteKey
import proton.android.pass.domain.key.ItemKey

class FakeOpenItemKey : OpenItemKey {

    var lastInviteKey: InviteKey? = null
        private set
    var lastEncryptedItemKey: EncryptedItemKey? = null
        private set

    private var result: ItemKey? = null

    fun setResult(value: ItemKey) {
        result = value
    }

    override fun invoke(inviteKey: InviteKey, key: EncryptedItemKey): ItemKey {
        lastInviteKey = inviteKey
        lastEncryptedItemKey = key
        return result ?: throw IllegalStateException("result not set")
    }
}
