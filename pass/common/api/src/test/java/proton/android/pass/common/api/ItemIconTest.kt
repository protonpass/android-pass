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

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Base64

class ItemIconTest {

    @Test
    fun `accepts valid data URIs`() {
        listOf(
            PNG_ICON,
            SVG_ICON,
            JPEG_ICON,
            WEBP_ICON,
            "data:image/png;base64,QUJD",
            "data:image/png;base64,QUI=",
            "data:image/png;base64,QQ=="
        ).forEach { icon ->
            assertThat(ItemIcon.isValid(icon)).isTrue()
        }
    }

    @Test
    fun `accepts data URI of exactly MAX_LENGTH`() {
        val prefix = "data:image/png;base64,"
        val icon = prefix + "A".repeat(ItemIcon.MAX_LENGTH - prefix.length)
        assertThat(icon.length).isEqualTo(ItemIcon.MAX_LENGTH)
        assertThat(ItemIcon.isValid(icon)).isTrue()
    }

    @Test
    fun `rejects data URI longer than MAX_LENGTH`() {
        val prefix = "data:image/png;base64,"
        val icon = prefix + "A".repeat(ItemIcon.MAX_LENGTH - prefix.length + 1)
        assertThat(ItemIcon.isValid(icon)).isFalse()
    }

    @Test
    fun `rejects anything that is not a strict base64 image data URI`() {
        listOf(
            null,
            "",
            "https://tracker.example.com/pixel.png",
            "http://tracker.example.com/pixel.png",
            "//tracker.example.com/pixel.png",
            "javascript:alert(1)",
            "javascript:data:image/png;base64,QUJD",
            "blob:https://example.com/0000-0000",
            "data:text/html;base64,${b64("<script>alert(1)</script>")}",
            "data:image/gif;base64,QUJD",
            "data:IMAGE/PNG;base64,QUJD",
            "data:image/svg+xml,<svg onload=\"alert(1)\"></svg>",
            "data:image/svg+xml;utf8,<svg></svg>",
            "data:image/png;charset=utf-8;base64,QUJD",
            "data:image/png;base64,QUJD<script>",
            "data:image/png;base64,QUJD QUJD",
            "data:image/png;base64,QUJD\nQUJD",
            "$PNG_ICON\n",
            "data:image/png;base64,QU-_QUJD",
            "data:image/png;base64,QQ===",
            "data:image/png;base64,QQ==QUJD",
            "data:image/png;base64,",
            " $PNG_ICON"
        ).forEach { icon ->
            assertThat(ItemIcon.isValid(icon)).isFalse()
            assertThat(ItemIcon.decode(icon)).isNull()
        }
    }

    @Test
    fun `decode returns mime type and bytes of valid icons`() {
        val decoded = ItemIcon.decode(PNG_ICON)

        assertThat(decoded).isNotNull()
        assertThat(decoded!!.mimeType).isEqualTo("image/png")
        assertThat(decoded.isSvg).isFalse()
        assertThat(decoded.bytes).isEqualTo(Base64.getDecoder().decode(PNG_B64))
    }

    @Test
    fun `decode flags svg icons`() {
        val decoded = ItemIcon.decode(SVG_ICON)

        assertThat(decoded).isNotNull()
        assertThat(decoded!!.isSvg).isTrue()
        assertThat(String(decoded.bytes)).isEqualTo(SVG)
    }

    @Test
    fun `decode returns null for a payload with an impossible base64 length`() {
        assertThat(ItemIcon.decode("data:image/png;base64,AAAAA")).isNull()
    }

    @Test
    fun `accepts png jpeg webp and svg`() {
        assertThat(ItemIcon.ACCEPTED_MIME_TYPES)
            .containsExactly("image/png", "image/jpeg", "image/webp", "image/svg+xml")
            .inOrder()
    }

    @Test
    fun `process rejects unaccepted mime types`() {
        listOf(null, "", "image/gif", "image/bmp", "text/html", "application/octet-stream", "image/svg")
            .forEach { mimeType ->
                val result = ItemIcon.process(mimeType, byteArrayOf(1)) { PNG_BYTES }
                assertThat(result).isEqualTo(ItemIconResult.Error(ItemIconError.Type))
            }
    }

    @Test
    fun `process rejects files larger than MAX_INPUT_SIZE`() {
        ItemIcon.ACCEPTED_MIME_TYPES.forEach { mimeType ->
            val result = ItemIcon.process(mimeType, ByteArray(ItemIcon.MAX_INPUT_SIZE + 1)) { PNG_BYTES }
            assertThat(result).isEqualTo(ItemIconResult.Error(ItemIconError.Size))
        }
    }

    @Test
    fun `process checks mime type before size`() {
        val result = ItemIcon.process("image/gif", ByteArray(ItemIcon.MAX_INPUT_SIZE + 1)) { PNG_BYTES }
        assertThat(result).isEqualTo(ItemIconResult.Error(ItemIconError.Type))
    }

    @Test
    fun `process rejects svg whose data URI would exceed MAX_LENGTH without rasterizing`() {
        var rasterizeCalled = false
        val bytes = ("<svg>" + " ".repeat(ItemIcon.MAX_LENGTH)).toByteArray()

        val result = ItemIcon.process(ItemIcon.SVG_MIME_TYPE, bytes) {
            rasterizeCalled = true
            PNG_BYTES
        }

        assertThat(result).isEqualTo(ItemIconResult.Error(ItemIconError.Size))
        assertThat(rasterizeCalled).isFalse()
    }

    @Test
    fun `process accepts svg whose data URI fits exactly in MAX_LENGTH`() {
        val prefixLength = "data:image/svg+xml;base64,".length
        val maxBytes = (ItemIcon.MAX_LENGTH - prefixLength) / 4 * 3

        val fits = ItemIcon.process(ItemIcon.SVG_MIME_TYPE, svgOfSize(maxBytes)) { null }
        val tooLarge = ItemIcon.process(ItemIcon.SVG_MIME_TYPE, svgOfSize(maxBytes + 1)) { null }

        assertThat(fits).isInstanceOf(ItemIconResult.Success::class.java)
        assertThat((fits as ItemIconResult.Success).icon.length).isAtMost(ItemIcon.MAX_LENGTH)
        assertThat(tooLarge).isEqualTo(ItemIconResult.Error(ItemIconError.Size))
    }

    @Test
    fun `maxInputSize is smaller for svg than for raster images`() {
        assertThat(ItemIcon.maxInputSize(ItemIcon.SVG_MIME_TYPE)).isEqualTo(ItemIcon.MAX_LENGTH * 3 / 4)
        assertThat(ItemIcon.maxInputSize("image/png")).isEqualTo(ItemIcon.MAX_INPUT_SIZE)
    }

    @Test
    fun `process keeps svg as vector data URI`() {
        val result = ItemIcon.process(ItemIcon.SVG_MIME_TYPE, SVG.toByteArray()) { error("must not rasterize svg") }
        assertThat(result).isEqualTo(ItemIconResult.Success(SVG_ICON))
    }

    @Test
    fun `process rejects svg files that are not svg documents`() {
        listOf("", "hello", "<html></html>", " <svg></svg>").forEach { content ->
            val result = ItemIcon.process(ItemIcon.SVG_MIME_TYPE, content.toByteArray()) { PNG_BYTES }
            assertThat(result).isEqualTo(ItemIconResult.Error(ItemIconError.Decode))
        }
    }

    @Test
    fun `process rasterizes raster images to png`() {
        listOf("image/png", "image/jpeg", "image/webp").forEach { mimeType ->
            var input: ByteArray? = null
            val source = byteArrayOf(1, 2, 3)

            val result = ItemIcon.process(mimeType, source) {
                input = it
                PNG_BYTES
            }

            assertThat(input).isEqualTo(source)
            assertThat(result).isEqualTo(ItemIconResult.Success(PNG_ICON))
        }
    }

    @Test
    fun `process returns decode error if the raster image cannot be decoded`() {
        val result = ItemIcon.process("image/jpeg", byteArrayOf(1, 2, 3)) { null }
        assertThat(result).isEqualTo(ItemIconResult.Error(ItemIconError.Decode))
    }

    @Test
    fun `process rejects rasterized output that exceeds MAX_LENGTH`() {
        val result = ItemIcon.process("image/png", byteArrayOf(1)) { ByteArray(ItemIcon.MAX_LENGTH) }
        assertThat(result).isEqualTo(ItemIconResult.Error(ItemIconError.Size))
    }

    @Test
    fun `dataUriLength matches the encoded length`() {
        listOf(0, 1, 2, 3, 4, 100, 1_000).forEach { size ->
            val bytes = ByteArray(size) { 1 }
            assertThat(ItemIcon.dataUriLength("image/png", size))
                .isEqualTo(ItemIcon.encode("image/png", bytes).length)
        }
    }

    private companion object {
        const val PNG_B64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
        const val SVG = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 1 1\">" +
            "<rect width=\"1\" height=\"1\"/></svg>"

        val PNG_BYTES: ByteArray = Base64.getDecoder().decode(PNG_B64)
        const val PNG_ICON = "data:image/png;base64,$PNG_B64"
        val SVG_ICON = "data:image/svg+xml;base64,${b64(SVG)}"
        const val JPEG_ICON = "data:image/jpeg;base64,$PNG_B64"
        const val WEBP_ICON = "data:image/webp;base64,$PNG_B64"

        fun b64(value: String): String = Base64.getEncoder().encodeToString(value.toByteArray())

        fun svgOfSize(size: Int): ByteArray {
            val body = "<svg></svg>"
            return (body + " ".repeat(size - body.length)).toByteArray()
        }
    }
}
