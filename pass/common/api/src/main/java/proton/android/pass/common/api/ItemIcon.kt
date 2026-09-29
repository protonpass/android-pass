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

package proton.android.pass.common.api

import java.util.Base64

/**
 * Custom item icons are stored inline in the item's protobuf `Metadata.icon` field as a
 * base64 image data URI, so no extra request is needed per item.
 *
 * SECURITY: items can be shared between users, so the icon is untrusted input. A malicious
 * sharer could set it to an `https://` URL (to track who opens the vault), to `javascript:`
 * or to any other scheme. Only strictly validated base64 image data URIs may be rendered,
 * and they are always decoded locally (see [decode]), never handed to a URL loader.
 */
object ItemIcon {

    /** Raster output size in pixels (square) */
    const val SIZE_PX: Int = 64

    /** Maximum source file size in bytes */
    const val MAX_INPUT_SIZE: Int = 512 * 1024

    /** Maximum data URI length stored in the item */
    const val MAX_LENGTH: Int = 32 * 1024

    const val PNG_MIME_TYPE: String = "image/png"
    const val SVG_MIME_TYPE: String = "image/svg+xml"

    val ACCEPTED_MIME_TYPES: List<String> = listOf(
        PNG_MIME_TYPE,
        "image/jpeg",
        "image/webp",
        SVG_MIME_TYPE
    )

    private const val BASE64_BLOCK_BYTES = 3
    private const val BASE64_BLOCK_CHARS = 4
    private const val SVG_SNIFF_LENGTH = 1024
    private const val DATA_PREFIX = "data:"

    private val DATA_URI_REGEX = Regex("data:image/(png|jpeg|webp|svg\\+xml);base64,[A-Za-z0-9+/]+={0,2}")

    /** Strict check: <= [MAX_LENGTH], base64 png/jpeg/webp/svg data URI, nothing else */
    fun isValid(icon: String?): Boolean = icon != null && icon.length <= MAX_LENGTH && DATA_URI_REGEX.matches(icon)

    fun isAcceptedMimeType(mimeType: String?): Boolean = mimeType in ACCEPTED_MIME_TYPES

    /**
     * SVGs are stored as-is (not rasterized), so their effective input limit is the maximum
     * stored data URI length minus the base64 overhead.
     */
    fun maxInputSize(mimeType: String?): Int = if (mimeType == SVG_MIME_TYPE) {
        MAX_LENGTH * BASE64_BLOCK_BYTES / BASE64_BLOCK_CHARS
    } else {
        MAX_INPUT_SIZE
    }

    /** Length of a base64 data URI for [byteLength] bytes of [mimeType] */
    fun dataUriLength(mimeType: String, byteLength: Int): Int {
        val base64Blocks = (byteLength + BASE64_BLOCK_BYTES - 1) / BASE64_BLOCK_BYTES
        return dataUriPrefix(mimeType).length + base64Blocks * BASE64_BLOCK_CHARS
    }

    fun encode(mimeType: String, bytes: ByteArray): String =
        dataUriPrefix(mimeType) + Base64.getEncoder().encodeToString(bytes)

    /**
     * Returns the decoded icon if [icon] is valid, otherwise null. This is the ONLY way
     * the UI should resolve an item icon to image data.
     */
    fun decode(icon: String?): DecodedItemIcon? {
        if (icon == null || !isValid(icon)) return null
        val mimeType = icon.substring(DATA_PREFIX.length, icon.indexOf(';'))
        val payload = icon.substring(icon.indexOf(',') + 1)
        val bytes = runCatching { Base64.getDecoder().decode(payload) }.getOrNull()
        if (bytes == null || bytes.isEmpty()) return null
        return DecodedItemIcon(mimeType = mimeType, bytes = bytes)
    }

    /**
     * Converts an image to an item icon data URI.
     * - SVG: kept as vector (original bytes base64-encoded).
     * - Raster: [rasterize] must center-crop and scale it to [SIZE_PX] and return PNG bytes,
     *   or null if the image cannot be decoded.
     * The mime type is checked before the size, and the output is always re-validated.
     */
    fun process(
        mimeType: String?,
        bytes: ByteArray,
        rasterize: (ByteArray) -> ByteArray?
    ): ItemIconResult {
        if (mimeType == null || !isAcceptedMimeType(mimeType)) return ItemIconResult.Error(ItemIconError.Type)
        if (bytes.size > MAX_INPUT_SIZE) return ItemIconResult.Error(ItemIconError.Size)

        val isSvg = mimeType == SVG_MIME_TYPE
        if (isSvg && dataUriLength(mimeType, bytes.size) > MAX_LENGTH) {
            return ItemIconResult.Error(ItemIconError.Size)
        }

        val icon = when {
            !isSvg -> rasterize(bytes)?.let { png -> encode(PNG_MIME_TYPE, png) }
            looksLikeSvg(bytes) -> encode(mimeType, bytes)
            else -> null
        }

        return when {
            icon == null -> ItemIconResult.Error(ItemIconError.Decode)
            icon.length > MAX_LENGTH -> ItemIconResult.Error(ItemIconError.Size)
            !isValid(icon) -> ItemIconResult.Error(ItemIconError.Decode)
            else -> ItemIconResult.Success(icon)
        }
    }

    /**
     * Same heuristic the image loader uses to pick its SVG decoder: the document must start
     * with `<` and contain an `<svg` tag near the beginning.
     */
    private fun looksLikeSvg(bytes: ByteArray): Boolean {
        if (bytes.isEmpty() || bytes[0] != '<'.code.toByte()) return false
        val head = String(bytes, 0, minOf(bytes.size, SVG_SNIFF_LENGTH), Charsets.UTF_8)
        return head.contains("<svg")
    }

    private fun dataUriPrefix(mimeType: String): String = "$DATA_PREFIX$mimeType;base64,"
}

data class DecodedItemIcon(val mimeType: String, val bytes: ByteArray) {

    val isSvg: Boolean = mimeType == ItemIcon.SVG_MIME_TYPE

    override fun equals(other: Any?): Boolean =
        other is DecodedItemIcon && mimeType == other.mimeType && bytes.contentEquals(other.bytes)

    override fun hashCode(): Int = 31 * mimeType.hashCode() + bytes.contentHashCode()
}

enum class ItemIconError {
    Type,
    Size,
    Decode
}

sealed interface ItemIconResult {
    data class Success(val icon: String) : ItemIconResult
    data class Error(val reason: ItemIconError) : ItemIconResult
}
