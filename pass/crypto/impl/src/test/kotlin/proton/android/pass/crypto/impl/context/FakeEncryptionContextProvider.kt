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

package proton.android.pass.crypto.impl.context

import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider

class FakeEncryptionContextProvider(private val key: EncryptionKey) : EncryptionContextProvider {
    override fun <R> withEncryptionContext(block: EncryptionContext.() -> R): R = withEncryptionContext(key, block)

    override fun <R> withEncryptionContext(key: EncryptionKey, block: EncryptionContext.() -> R): R =
        block(EncryptionContextImpl(key))

    override suspend fun <R> withEncryptionContextSuspendable(block: suspend EncryptionContext.() -> R): R =
        withEncryptionContextSuspendable(key, block)

    override suspend fun <R> withEncryptionContextSuspendable(
        key: EncryptionKey,
        block: suspend EncryptionContext.() -> R
    ): R = block(EncryptionContextImpl(key))
}
