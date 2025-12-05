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

import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.usecases.EncryptedItemRevision
import proton.android.pass.crypto.api.usecases.OpenItem
import proton.android.pass.crypto.api.usecases.OpenItemOutput
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.domain.Share
import proton.android.pass.domain.key.ShareKey

class FakeOpenItem : OpenItem {

    private var output: OpenItemOutput? = null

    fun setOutput(value: OpenItemOutput) {
        output = value
    }

    override fun open(
        response: EncryptedItemRevision,
        share: Share,
        shareKeys: List<ShareKey>
    ): OpenItemOutput = open(response, share, shareKeys, FakeEncryptionContext)

    override fun open(
        response: EncryptedItemRevision,
        share: Share,
        shareKeys: List<ShareKey>,
        encryptionContext: EncryptionContext
    ): OpenItemOutput = output ?: throw IllegalStateException("output not set")
}
