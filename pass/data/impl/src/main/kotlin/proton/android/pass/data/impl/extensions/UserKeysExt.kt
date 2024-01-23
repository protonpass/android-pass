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

package proton.android.pass.data.impl.extensions

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.entity.keyholder.KeyHolder
import me.proton.core.key.domain.entity.keyholder.KeyHolderContext
import me.proton.core.key.domain.useKeys
import proton.android.pass.log.api.PassLogger

private const val TAG = "UserKeysExt"

fun <T> KeyHolder.tryUseKeys(
    message: String,
    cryptoContext: CryptoContext,
    block: KeyHolderContext.() -> T
): T = runCatching {
    useKeys(cryptoContext) { block() }
}.getOrElse {
    PassLogger.w(TAG, "Error using user keys for $message")
    PassLogger.e(TAG, it)
    throw it
}

