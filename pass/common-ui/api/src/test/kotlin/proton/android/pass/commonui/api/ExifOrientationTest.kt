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

package proton.android.pass.commonui.api

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExifOrientationTest {

    @Test
    fun `reads orientations 1, 3, 6 and 8 from little endian EXIF`() {
        listOf(1, 3, 6, 8).forEach { orientation ->
            val jpeg = jpeg(app1(tiff(littleEndian = true, orientation = orientation)))
            assertThat(ExifOrientation.read(jpeg)).isEqualTo(orientation)
        }
    }

    @Test
    fun `reads orientations 1, 3, 6 and 8 from big endian EXIF`() {
        listOf(1, 3, 6, 8).forEach { orientation ->
            val jpeg = jpeg(app1(tiff(littleEndian = false, orientation = orientation)))
            assertThat(ExifOrientation.read(jpeg)).isEqualTo(orientation)
        }
    }

    @Test
    fun `finds EXIF after other segments, fill bytes and other IFD entries`() {
        val tiff = tiff(littleEndian = true, orientation = 6, entriesBefore = 3)
        val jpeg = jpeg(app0(), bytes(0xFF, 0xFF), app1(tiff))

        assertThat(ExifOrientation.read(jpeg)).isEqualTo(6)
    }

    @Test
    fun `returns normal when there is no EXIF segment`() {
        assertThat(ExifOrientation.read(jpeg(app0()))).isEqualTo(1)
    }

    @Test
    fun `returns normal for non JPEG and empty input`() {
        val png = bytes(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)

        assertThat(ExifOrientation.read(png)).isEqualTo(1)
        assertThat(ExifOrientation.read(ByteArray(0))).isEqualTo(1)
        assertThat(ExifOrientation.read(bytes(0xFF))).isEqualTo(1)
    }

    @Test
    fun `returns normal for every truncation of the EXIF segment`() {
        val jpeg = jpeg(app0(), app1(tiff(littleEndian = true, orientation = 6)))
        val app1End = jpeg.size - SOS_SIZE

        for (length in 0 until app1End) {
            assertThat(ExifOrientation.read(jpeg.copyOf(length))).isEqualTo(1)
        }
        assertThat(ExifOrientation.read(jpeg.copyOf(app1End))).isEqualTo(6)
    }

    @Test
    fun `returns normal when the segment length points past the data`() {
        val jpeg = bytes(0xFF, 0xD8, 0xFF, 0xE1, 0xFF, 0xFF) + EXIF_HEADER + tiff(true, 6)

        assertThat(ExifOrientation.read(jpeg)).isEqualTo(1)
    }

    @Test
    fun `returns normal when the segment length is too small`() {
        assertThat(ExifOrientation.read(bytes(0xFF, 0xD8, 0xFF, 0xE1, 0x00, 0x01))).isEqualTo(1)
    }

    @Test
    fun `stops at the start of scan`() {
        val jpeg = bytes(0xFF, 0xD8, 0xFF, 0xDA, 0x00, 0x02) + app1(tiff(true, 6))

        assertThat(ExifOrientation.read(jpeg)).isEqualTo(1)
    }

    @Test
    fun `returns normal for an invalid TIFF header`() {
        val badByteOrder = tiff(true, 6).also { it[0] = 0x41; it[1] = 0x41 }
        val badMagic = tiff(true, 6).also { it[2] = 0x2B }

        assertThat(ExifOrientation.read(jpeg(app1(badByteOrder)))).isEqualTo(1)
        assertThat(ExifOrientation.read(jpeg(app1(badMagic)))).isEqualTo(1)
    }

    @Test
    fun `returns normal when the IFD offset points outside of the segment`() {
        listOf(0x0000_0100L, 0xFFFF_FFFFL, 0x0000_0004L).forEach { ifdOffset ->
            val jpeg = jpeg(app1(tiff(littleEndian = true, orientation = 6, ifdOffset = ifdOffset)))
            assertThat(ExifOrientation.read(jpeg)).isEqualTo(1)
        }
    }

    @Test
    fun `returns normal when the IFD declares more entries than present`() {
        val tiff = tiff(
            littleEndian = true,
            orientation = 6,
            entriesBefore = 2,
            declaredEntries = 0xFFFF,
            orientationTag = 0x0131
        )

        assertThat(ExifOrientation.read(jpeg(app1(tiff)))).isEqualTo(1)
    }

    @Test
    fun `returns normal when the orientation entry has an unexpected type or count`() {
        val wrongType = tiff(littleEndian = true, orientation = 6, type = 4)
        val wrongCount = tiff(littleEndian = true, orientation = 6, count = 2)

        assertThat(ExifOrientation.read(jpeg(app1(wrongType)))).isEqualTo(1)
        assertThat(ExifOrientation.read(jpeg(app1(wrongCount)))).isEqualTo(1)
    }

    @Test
    fun `returns normal for out of range orientation values`() {
        listOf(0, 9, 0xFFFF).forEach { orientation ->
            val jpeg = jpeg(app1(tiff(littleEndian = true, orientation = orientation)))
            assertThat(ExifOrientation.read(jpeg)).isEqualTo(1)
        }
    }

    @Test
    fun `maps orientations to transforms`() {
        val expected = mapOf(
            1 to ExifTransform(0, mirrorHorizontally = false),
            2 to ExifTransform(0, mirrorHorizontally = true),
            3 to ExifTransform(180, mirrorHorizontally = false),
            4 to ExifTransform(180, mirrorHorizontally = true),
            5 to ExifTransform(90, mirrorHorizontally = true),
            6 to ExifTransform(90, mirrorHorizontally = false),
            7 to ExifTransform(270, mirrorHorizontally = true),
            8 to ExifTransform(270, mirrorHorizontally = false),
            0 to ExifTransform(0, mirrorHorizontally = false),
            9 to ExifTransform(0, mirrorHorizontally = false)
        )

        expected.forEach { (orientation, transform) ->
            assertThat(ExifOrientation.transformFor(orientation)).isEqualTo(transform)
        }
        assertThat(ExifOrientation.transformFor(1).isIdentity).isTrue()
        assertThat(ExifOrientation.transformFor(6).isIdentity).isFalse()
    }

    /**
     * Builds the pixels a camera stores for an upright image, following the EXIF definition of
     * where the stored row 0 / column 0 end up, and checks that the transform makes it upright.
     */
    @Test
    fun `transforms turn stored pixels upright`() {
        val width = 3
        val height = 2
        val upright = List(height) { y -> List(width) { x -> y * width + x } }

        for (orientation in 1..8) {
            val stored = storedPixels(upright, orientation)
            val transform = ExifOrientation.transformFor(orientation)

            var image = stored
            repeat(transform.rotationDegrees / 90) { image = rotateClockwise(image) }
            if (transform.mirrorHorizontally) image = image.map { it.reversed() }

            assertThat(image).isEqualTo(upright)
        }
    }

    /** Maps each stored pixel (row, column) to the upright pixel that the EXIF orientation places there */
    private fun storedPixels(upright: List<List<Int>>, orientation: Int): List<List<Int>> {
        val height = upright.size
        val width = upright.first().size
        val transposed = orientation >= 5
        val rows = if (transposed) width else height
        val columns = if (transposed) height else width
        return List(rows) { r ->
            List(columns) { c ->
                val (x, y) = when (orientation) {
                    1 -> c to r
                    2 -> width - 1 - c to r
                    3 -> width - 1 - c to height - 1 - r
                    4 -> c to height - 1 - r
                    5 -> r to c
                    6 -> width - 1 - r to c
                    7 -> width - 1 - r to height - 1 - c
                    8 -> r to height - 1 - c
                    else -> error("Unexpected orientation $orientation")
                }
                upright[y][x]
            }
        }
    }

    /** Visual clockwise rotation (y axis pointing down, as on screen and in `Matrix.setRotate`) */
    private fun rotateClockwise(image: List<List<Int>>): List<List<Int>> {
        val height = image.size
        val width = image.first().size
        return List(width) { y -> List(height) { x -> image[height - 1 - x][y] } }
    }

    private fun jpeg(vararg segments: ByteArray): ByteArray =
        segments.fold(bytes(0xFF, 0xD8)) { acc, segment -> acc + segment } + bytes(0xFF, 0xDA, 0x00, 0x02)

    private fun app0(): ByteArray {
        val payload = "JFIF".toByteArray() + bytes(0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00)
        return bytes(0xFF, 0xE0) + u16(payload.size + 2, littleEndian = false) + payload
    }

    private fun app1(tiff: ByteArray): ByteArray {
        val payload = EXIF_HEADER + tiff
        return bytes(0xFF, 0xE1) + u16(payload.size + 2, littleEndian = false) + payload
    }

    @Suppress("LongParameterList")
    private fun tiff(
        littleEndian: Boolean,
        orientation: Int,
        entriesBefore: Int = 0,
        declaredEntries: Int = entriesBefore + 1,
        ifdOffset: Long = 8,
        type: Int = 3,
        count: Long = 1,
        orientationTag: Int = 0x0112
    ): ByteArray {
        val header = (if (littleEndian) bytes(0x49, 0x49) else bytes(0x4D, 0x4D)) +
            u16(0x2A, littleEndian) +
            u32(ifdOffset, littleEndian)
        // Other SHORT tags sorted before the orientation one: ImageWidth, ImageLength, BitsPerSample
        val otherEntries = (0 until entriesBefore).fold(ByteArray(0)) { acc, index ->
            acc + entry(0x0100 + index, type = 3, count = 1, value = 0x40, littleEndian = littleEndian)
        }
        val orientationEntry = entry(orientationTag, type, count, orientation, littleEndian)
        return header + u16(declaredEntries, littleEndian) + otherEntries + orientationEntry +
            u32(0, littleEndian)
    }

    private fun entry(
        tag: Int,
        type: Int,
        count: Long,
        value: Int,
        littleEndian: Boolean
    ): ByteArray = u16(tag, littleEndian) + u16(type, littleEndian) + u32(count, littleEndian) +
        u16(value, littleEndian) + u16(0, littleEndian)

    private fun u16(value: Int, littleEndian: Boolean): ByteArray {
        val big = bytes(value shr 8 and 0xFF, value and 0xFF)
        return if (littleEndian) big.reversedArray() else big
    }

    private fun u32(value: Long, littleEndian: Boolean): ByteArray {
        val big = bytes(
            (value shr 24 and 0xFF).toInt(),
            (value shr 16 and 0xFF).toInt(),
            (value shr 8 and 0xFF).toInt(),
            (value and 0xFF).toInt()
        )
        return if (littleEndian) big.reversedArray() else big
    }

    private fun bytes(vararg values: Int): ByteArray = ByteArray(values.size) { values[it].toByte() }

    private companion object {
        const val SOS_SIZE = 4
        val EXIF_HEADER = byteArrayOf(0x45, 0x78, 0x69, 0x66, 0x00, 0x00)
    }
}
