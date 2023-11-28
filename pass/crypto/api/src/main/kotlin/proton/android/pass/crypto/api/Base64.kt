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

    enum class Mode {
        UrlSafe,
        Standard
    }

    fun encodeBase64String(array: ByteArray, mode: Mode = Mode.Standard): EncryptedString =
        String(encodeBase64(array, mode))

    fun decodeBase64(content: EncryptedString): ByteArray = decodeBase64(content.toByteArray())

    fun encodeBase64(array: ByteArray, mode: Mode = Mode.Standard): ByteArray = runCatching {
        when (mode) {
            Mode.Standard -> org.apache.commons.codec.binary.Base64.encodeBase64(array)
            Mode.UrlSafe -> org.apache.commons.codec.binary.Base64.encodeBase64URLSafe(array)
        }
    }.getOrElse {
        if (it is NoSuchMethodError) {
            // This is a workaround for implementations that don't have the proper support for B64
            Base64Fallback.encodeBase64(array, mode)
        } else {
            throw it
        }
    }

    fun decodeBase64(array: ByteArray, mode: Mode = Mode.Standard): ByteArray = runCatching {
        // We don't need to use the mode here, as the commons implementation is able to detect
        // which encoding is used
        org.apache.commons.codec.binary.Base64.decodeBase64(array)
    }.getOrElse { error ->
        if (error is NoSuchMethodError) {
            // This is a workaround for implementations that don't have the proper support for B64
            Base64Fallback.decodeBase64(array, mode)
        } else {
            throw error
        }
    }
}

object Base64Fallback {
    fun encodeBase64(array: ByteArray, mode: Base64.Mode): ByteArray = when (mode) {
        Base64.Mode.Standard -> KotlinBase64.encodeToByteArray(array)
        Base64.Mode.UrlSafe -> KotlinBase64.UrlSafe.encodeToByteArray(array)

    }
    fun decodeBase64(array: ByteArray, mode: Base64.Mode): ByteArray = runCatching {
        when (mode) {
            Base64.Mode.Standard -> KotlinBase64.decode(array)
            Base64.Mode.UrlSafe -> KotlinBase64.UrlSafe.decode(array)
        }
    }.getOrElse {
        KotlinBase64.decode(array)
    }
}
