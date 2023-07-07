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

package proton.android.pass.crypto.api

import me.proton.core.crypto.common.keystore.EncryptedString

object Base64 {

    fun encodeBase64String(array: ByteArray): EncryptedString = String(encodeBase64(array))

    fun decodeBase64(content: EncryptedString): ByteArray = decodeBase64(content.toByteArray())

    fun encodeBase64(array: ByteArray): ByteArray = runCatching {
        org.apache.commons.codec.binary.Base64.encodeBase64(array)
    }.fold(
        onSuccess = { it },
        onFailure = {
            if (it is NoSuchMethodError) {
                // This is a workaround for implementations that don't have the proper support for B64
                Base64Fallback.encodeBase64(array)
            } else {
                throw it
            }
        }
    )

    fun decodeBase64(array: ByteArray): ByteArray = runCatching {
        org.apache.commons.codec.binary.Base64.decodeBase64(array)
    }.fold(
        onSuccess = { it },
        onFailure = { error ->
            if (error is NoSuchMethodError) {
                // This is a workaround for implementations that don't have the proper support for B64
                Base64Fallback.decodeBase64(array)
            } else {
                throw error
            }
        }
    )
}

object Base64Fallback {
    fun encodeBase64(array: ByteArray): ByteArray = KotlinBase64.UrlSafe.encodeToByteArray(array)
    fun decodeBase64(array: ByteArray): ByteArray = runCatching {
        KotlinBase64.UrlSafe.decode(array)
    }.fold(
        onSuccess = { it },
        onFailure = {
            KotlinBase64.decode(array)
        }
    )
}
