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

import java.security.MessageDigest
import java.util.Base64

/**
 * Custom item icons are stored inline in the item's protobuf `Metadata.icon` field as a
 * base64 image data URI, so no extra request is needed per item.
 *
 * SECURITY: items can be shared between users, so the icon is untrusted input. A malicious
 * sharer could set it to an `https://` URL (to track who opens the vault), to `javascript:`
 * or to any other scheme. Only strictly validated base64 image data URIs may be rendered,
 * and they are always decoded locally (see [decode]), never handed to a URL loader.
 * [decode] also checks the content: raster images must match their declared type and SVGs
 * must pass [isSafeSvg], because the SVG renderer has no resource limits of its own.
 */
object ItemIcon {

    /** Raster output size in pixels (square) */
    const val SIZE_PX: Int = 64

    /** Maximum source file size in bytes */
    const val MAX_INPUT_SIZE: Int = 512 * 1024

    /** Maximum data URI length stored in the item */
    const val MAX_LENGTH: Int = 32 * 1024

    const val PNG_MIME_TYPE: String = "image/png"
    const val JPEG_MIME_TYPE: String = "image/jpeg"
    const val WEBP_MIME_TYPE: String = "image/webp"
    const val SVG_MIME_TYPE: String = "image/svg+xml"

    val ACCEPTED_MIME_TYPES: List<String> = listOf(
        PNG_MIME_TYPE,
        JPEG_MIME_TYPE,
        WEBP_MIME_TYPE,
        SVG_MIME_TYPE
    )

    private const val BASE64_BLOCK_BYTES = 3
    private const val BASE64_BLOCK_CHARS = 4
    private const val DATA_PREFIX = "data:"
    private const val MAX_SVG_USE_ELEMENTS = 50
    private const val SVG_ROOT_TAG = "<svg"
    private const val NON_STANDARD_JPEG_MIME_TYPE = "image/jpg"

    private val DATA_URI_REGEX = Regex("data:image/(png|jpeg|webp|svg\\+xml);base64,[A-Za-z0-9+/]+={0,2}")

    private val UTF8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    private val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    private val JPEG_SIGNATURE = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
    private val RIFF_SIGNATURE = "RIFF".toByteArray(Charsets.US_ASCII)
    private val WEBP_SIGNATURE = "WEBP".toByteArray(Charsets.US_ASCII)
    private const val WEBP_SIGNATURE_OFFSET = 8

    private val XML_ENCODING_REGEX = Regex(
        "^\\s*<\\?xml[^>]*\\bencoding\\s*=\\s*[\"']([^\"']*)[\"']",
        RegexOption.IGNORE_CASE
    )
    private val SVG_ALLOWED_ENCODINGS = setOf("utf-8", "utf8", "us-ascii", "ascii")

    /**
     * Constructs the SVG renderer would resolve (embedded images, which it decodes without any
     * size limit), that could reference external resources, or that expand entities. An element
     * may have a namespace prefix, so `<svg:image` must be caught as well as `<image`.
     */
    private val SVG_FORBIDDEN_REGEXES = listOf(
        Regex("<(?:[\\w.-]+:)?(?:image|foreignObject)[\\s>/]", RegexOption.IGNORE_CASE),
        Regex("<!DOCTYPE", RegexOption.IGNORE_CASE),
        Regex("<!ENTITY", RegexOption.IGNORE_CASE),
        Regex("@import", RegexOption.IGNORE_CASE),
        // Only same-document references (`href="#id"`) are allowed
        Regex("href\\s*=\\s*[\"'](?!\\s*#)", RegexOption.IGNORE_CASE)
    )
    private val SVG_USE_REGEX = Regex("<(?:[\\w.-]+:)?use[\\s>/]", RegexOption.IGNORE_CASE)

    /** Format check only: <= [MAX_LENGTH], base64 png/jpeg/webp/svg data URI, nothing else */
    fun isValid(icon: String?): Boolean = icon != null && icon.length <= MAX_LENGTH && DATA_URI_REGEX.matches(icon)

    /** A blank icon is the same as no icon */
    fun normalize(icon: String?): String? = icon?.takeIf { it.isNotBlank() }

    /**
     * Returns why a stored icon cannot be kept, or null if it is fine or not set. Mirrors the web
     * form validation: the length is checked first, then the format and content.
     */
    fun validate(icon: String?): ItemIconError? {
        val value = normalize(icon) ?: return null
        return when {
            value.length > MAX_LENGTH -> ItemIconError.Size
            decode(value) == null -> ItemIconError.Decode
            else -> null
        }
    }

    fun isAcceptedMimeType(mimeType: String?): Boolean = mimeType in ACCEPTED_MIME_TYPES

    /** Lowercases [mimeType], drops its parameters and maps the non-standard `image/jpg` to `image/jpeg` */
    fun normalizeMimeType(mimeType: String?): String? = mimeType
        ?.substringBefore(';')
        ?.trim()
        ?.lowercase()
        ?.takeIf { it.isNotEmpty() }
        ?.let { if (it == NON_STANDARD_JPEG_MIME_TYPE) JPEG_MIME_TYPE else it }

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
     * Returns the decoded icon if [icon] is valid and its content is safe to render, otherwise
     * null. This is the ONLY way the UI should resolve an item icon to image data.
     */
    fun decode(icon: String?): DecodedItemIcon? {
        if (icon == null || !isValid(icon)) return null
        val mimeType = icon.substring(DATA_PREFIX.length, icon.indexOf(';'))
        val payload = icon.substring(icon.indexOf(',') + 1)
        val bytes = runCatching { Base64.getDecoder().decode(payload) }.getOrNull()
        if (bytes == null || bytes.isEmpty()) return null
        return when {
            mimeType != SVG_MIME_TYPE -> bytes.takeIf { hasMagicBytes(mimeType, it) }
            // The XML parser does not accept anything before the XML declaration
            isSafeSvg(bytes) -> bytes.copyOfRange(svgStart(bytes), bytes.size)
            else -> null
        }?.let { content -> DecodedItemIcon(mimeType = mimeType, bytes = content) }
    }

    /**
     * Converts an image to an item icon data URI.
     * - SVG: kept as vector (original bytes base64-encoded) if it passes [isSafeSvg] and
     *   [isRenderableSvg] (a structural parse done by the caller, given the bytes to render).
     * - Raster: [rasterize] must center-crop and scale it to [SIZE_PX] and return PNG bytes,
     *   or null if the image cannot be decoded.
     * The mime type is checked before the size, and the output is always re-validated.
     */
    fun process(
        mimeType: String?,
        bytes: ByteArray,
        isRenderableSvg: (ByteArray) -> Boolean = { true },
        rasterize: (ByteArray) -> ByteArray?
    ): ItemIconResult {
        if (mimeType == null || !isAcceptedMimeType(mimeType)) return ItemIconResult.Error(ItemIconError.Type)
        if (bytes.size > MAX_INPUT_SIZE) return ItemIconResult.Error(ItemIconError.Size)

        val isSvg = mimeType == SVG_MIME_TYPE
        if (isSvg && dataUriLength(mimeType, bytes.size) > MAX_LENGTH) {
            return ItemIconResult.Error(ItemIconError.Size)
        }

        val icon = if (isSvg) encode(mimeType, bytes) else rasterize(bytes)?.let { png -> encode(PNG_MIME_TYPE, png) }
        val decodedIcon = icon?.takeIf { it.length <= MAX_LENGTH }?.let(::decode)

        return when {
            icon == null -> ItemIconResult.Error(ItemIconError.Decode)
            icon.length > MAX_LENGTH -> ItemIconResult.Error(ItemIconError.Size)
            decodedIcon == null -> ItemIconResult.Error(ItemIconError.Decode)
            decodedIcon.isSvg && !isRenderableSvg(decodedIcon.bytes) -> ItemIconResult.Error(ItemIconError.Decode)
            else -> ItemIconResult.Success(icon)
        }
    }

    /**
     * Lightweight pre-screen for untrusted SVGs, run before the SVG renderer sees them.
     * The document must be UTF-8 (optionally with a BOM), start with an `<svg` root element
     * (after optional whitespace, XML declaration and comments), and must not contain
     * embedded images, foreign objects, DTDs/entities, CSS imports, external references or
     * more than [MAX_SVG_USE_ELEMENTS] `<use>` elements.
     */
    fun isSafeSvg(bytes: ByteArray): Boolean {
        val content = svgText(bytes) ?: return false
        if (!hasSvgRoot(content)) return false
        if (SVG_FORBIDDEN_REGEXES.any { it.containsMatchIn(content) }) return false
        return SVG_USE_REGEX.findAll(content).take(MAX_SVG_USE_ELEMENTS + 1).count() <= MAX_SVG_USE_ELEMENTS
    }

    /** Checks that raster bytes start with the signature of the declared [mimeType] */
    fun hasMagicBytes(mimeType: String, bytes: ByteArray): Boolean = when (mimeType) {
        PNG_MIME_TYPE -> bytes.startsWith(PNG_SIGNATURE)
        JPEG_MIME_TYPE -> bytes.startsWith(JPEG_SIGNATURE)
        WEBP_MIME_TYPE -> bytes.startsWith(RIFF_SIGNATURE) &&
            bytes.startsWith(WEBP_SIGNATURE, offset = WEBP_SIGNATURE_OFFSET)
        else -> false
    }

    /**
     * Decodes the SVG as UTF-8 without its BOM. Other encodings are rejected: the XML parser
     * would honour them, so the text checks in [isSafeSvg] could be bypassed.
     */
    private fun svgText(bytes: ByteArray): String? {
        val offset = if (bytes.startsWith(UTF8_BOM)) UTF8_BOM.size else 0
        if ((offset until bytes.size).any { bytes[it] == 0.toByte() }) return null
        val content = String(bytes, offset, bytes.size - offset, Charsets.UTF_8)
        val encoding = XML_ENCODING_REGEX.find(content)?.groupValues?.get(1)?.trim()?.lowercase()
        return content.takeIf { encoding == null || encoding in SVG_ALLOWED_ENCODINGS }
    }

    /**
     * The root element must be `<svg`, after optional whitespace, XML declaration, processing
     * instructions and comments. Scanned by hand: a regex with a repeated group recurses per
     * iteration and could overflow the stack on a long crafted prolog.
     */
    private fun hasSvgRoot(content: String): Boolean {
        var index = content.skipWhitespace(0)
        while (true) {
            val end = when {
                content.startsWith("<?", index) -> content.indexOf("?>", index + 2).takeIf { it >= 0 }?.plus(2)
                content.startsWith("<!--", index) -> content.indexOf("-->", index + 4).takeIf { it >= 0 }?.plus(3)
                else -> break
            } ?: return false
            index = content.skipWhitespace(end)
        }
        val next = content.getOrNull(index + SVG_ROOT_TAG.length)
        return content.startsWith(SVG_ROOT_TAG, index) && next != null && (next.isXmlWhitespace() || next in "/>")
    }

    private fun String.skipWhitespace(from: Int): Int {
        var index = from
        while (index < length && this[index].isXmlWhitespace()) index++
        return index
    }

    private fun Char.isXmlWhitespace(): Boolean = this == ' ' || this == '\t' || this == '\n' || this == '\r'

    /** Offset of the first byte after the optional UTF-8 BOM and leading whitespace */
    private fun svgStart(bytes: ByteArray): Int {
        var index = if (bytes.startsWith(UTF8_BOM)) UTF8_BOM.size else 0
        while (index < bytes.size && bytes[index].toInt().toChar().isXmlWhitespace()) index++
        return index
    }

    private fun ByteArray.startsWith(prefix: ByteArray, offset: Int = 0): Boolean =
        size >= offset + prefix.size && prefix.indices.all { this[offset + it] == prefix[it] }

    private fun dataUriPrefix(mimeType: String): String = "$DATA_PREFIX$mimeType;base64,"
}

data class DecodedItemIcon(val mimeType: String, val bytes: ByteArray) {

    val isSvg: Boolean = mimeType == ItemIcon.SVG_MIME_TYPE

    /** Stable key for image caches: a hash of the type and content, never the content itself */
    val cacheKey: String by lazy {
        val digest = MessageDigest.getInstance("SHA-256").run {
            update(mimeType.toByteArray(Charsets.UTF_8))
            update(bytes)
            digest()
        }
        "item-icon:" + digest.joinToString(separator = "") { "%02x".format(it) }
    }

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
