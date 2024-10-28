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
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.encoding.Base64 as KBase64

object Base64 {

    enum class Mode {
        UrlSafe,
        Standard
    }

    fun encodeBase64String(array: ByteArray, mode: Mode = Mode.Standard): EncryptedString =
        String(encodeBase64(array, mode))

    fun decodeBase64(content: EncryptedString): ByteArray = decodeBase64(content.toByteArray())

    fun encodeBase64(array: ByteArray, mode: Mode = Mode.Standard): ByteArray = KotlinBase64.encodeBase64(array, mode)

    fun decodeBase64(array: ByteArray, mode: Mode = Mode.Standard): ByteArray = KotlinBase64.decodeBase64(array, mode)
}

@OptIn(ExperimentalEncodingApi::class)
object KotlinBase64 {
    fun encodeBase64(array: ByteArray, mode: Base64.Mode): ByteArray = when (mode) {
        Base64.Mode.Standard -> KBase64.encodeToByteArray(array)
        Base64.Mode.UrlSafe -> KBase64.UrlSafe.encodeToByteArray(array)
    }

    fun decodeBase64(array: ByteArray, mode: Base64.Mode): ByteArray = runCatching {
        when (mode) {
            Base64.Mode.Standard -> KBase64.decode(array)
            Base64.Mode.UrlSafe -> KBase64.UrlSafe.decode(array)
        }
    }.getOrElse {
        KBase64.decode(array)
    }
}
