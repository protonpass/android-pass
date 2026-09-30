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

/**
 * How to draw a decoded image so that it appears upright: rotate it clockwise by
 * [rotationDegrees] first, then mirror it horizontally if [mirrorHorizontally] is set.
 */
internal data class ExifTransform(val rotationDegrees: Int, val mirrorHorizontally: Boolean) {
    val isIdentity: Boolean get() = rotationDegrees == 0 && !mirrorHorizontally
}

/**
 * Minimal reader for the EXIF Orientation tag (0x0112) of a JPEG, used where the platform decoder
 * does not apply it (`BitmapFactory`, before API 28). It only looks at IFD0 of the first `Exif`
 * APP1 segment, never reads outside of the given bytes and treats anything malformed as
 * [ORIENTATION_NORMAL].
 */
internal object ExifOrientation {

    const val ORIENTATION_NORMAL = 1
    private const val ORIENTATION_FLIP_HORIZONTAL = 2
    private const val ORIENTATION_ROTATE_180 = 3
    private const val ORIENTATION_FLIP_VERTICAL = 4
    private const val ORIENTATION_TRANSPOSE = 5
    private const val ORIENTATION_ROTATE_90 = 6
    private const val ORIENTATION_TRANSVERSE = 7
    private const val ORIENTATION_ROTATE_270 = 8

    private const val MARKER_PREFIX = 0xFF
    private const val MARKER_SOI = 0xD8
    private const val MARKER_APP1 = 0xE1
    private const val MARKER_SOS = 0xDA
    private const val MARKER_EOI = 0xD9
    private const val MARKER_TEM = 0x01
    private const val MARKER_RST_FIRST = 0xD0
    private const val MARKER_RST_LAST = 0xD7

    private const val SEGMENT_LENGTH_SIZE = 2
    private const val MAX_SEGMENTS = 64

    private val EXIF_HEADER = byteArrayOf(0x45, 0x78, 0x69, 0x66, 0x00, 0x00) // "Exif\0\0"
    private const val BYTE_ORDER_LITTLE_ENDIAN = 0x4949 // "II"
    private const val BYTE_ORDER_BIG_ENDIAN = 0x4D4D // "MM"
    private const val TIFF_MAGIC = 0x002A
    private const val TIFF_HEADER_SIZE = 8
    private const val TIFF_MAGIC_POSITION = 2
    private const val TIFF_IFD_OFFSET_POSITION = 4
    private const val IFD_COUNT_SIZE = 2
    private const val ENTRY_TYPE_POSITION = 2
    private const val ENTRY_COUNT_POSITION = 4
    private const val ENTRY_VALUE_POSITION = 8
    private const val IFD_ENTRY_SIZE = 12
    private const val TAG_ORIENTATION = 0x0112
    private const val TYPE_SHORT = 3

    private const val BYTE_MASK = 0xFF
    private const val BITS_PER_BYTE = 8

    private const val DEGREES_90 = 90
    private const val DEGREES_180 = 180
    private const val DEGREES_270 = 270

    /** Returns the EXIF orientation (1 to 8) of a JPEG, or [ORIENTATION_NORMAL] if it has none */
    fun read(bytes: ByteArray): Int = findExifSegment(bytes)
        ?.let { (start, end) -> readTiffOrientation(bytes, start, end) }
        ?.takeIf { it in ORIENTATION_NORMAL..ORIENTATION_ROTATE_270 }
        ?: ORIENTATION_NORMAL

    /** Same mapping as `androidx.exifinterface` and Glide use to build their orientation matrix */
    fun transformFor(orientation: Int): ExifTransform = when (orientation) {
        ORIENTATION_FLIP_HORIZONTAL -> ExifTransform(0, mirrorHorizontally = true)
        ORIENTATION_ROTATE_180 -> ExifTransform(DEGREES_180, mirrorHorizontally = false)
        ORIENTATION_FLIP_VERTICAL -> ExifTransform(DEGREES_180, mirrorHorizontally = true)
        ORIENTATION_TRANSPOSE -> ExifTransform(DEGREES_90, mirrorHorizontally = true)
        ORIENTATION_ROTATE_90 -> ExifTransform(DEGREES_90, mirrorHorizontally = false)
        ORIENTATION_TRANSVERSE -> ExifTransform(DEGREES_270, mirrorHorizontally = true)
        ORIENTATION_ROTATE_270 -> ExifTransform(DEGREES_270, mirrorHorizontally = false)
        else -> ExifTransform(0, mirrorHorizontally = false)
    }

    /** Returns the [start, end) range of the TIFF data inside the first `Exif` APP1 segment */
    @Suppress("ReturnCount")
    private fun findExifSegment(bytes: ByteArray): Pair<Int, Int>? {
        if (bytes.u8(0) != MARKER_PREFIX || bytes.u8(1) != MARKER_SOI) return null
        var offset = 2
        repeat(MAX_SEGMENTS) {
            if (bytes.u8(offset) != MARKER_PREFIX) return null
            // Markers may be preceded by any number of 0xFF fill bytes
            while (bytes.u8(offset) == MARKER_PREFIX) offset++
            val marker = bytes.u8(offset) ?: return null
            offset++
            when (marker) {
                MARKER_SOS, MARKER_EOI -> return null
                MARKER_TEM, in MARKER_RST_FIRST..MARKER_RST_LAST -> return@repeat // No length
            }
            val length = bytes.u16(offset, littleEndian = false) ?: return null
            if (length < SEGMENT_LENGTH_SIZE) return null
            val dataStart = offset + SEGMENT_LENGTH_SIZE
            val segmentEnd = offset.toLong() + length
            if (segmentEnd > bytes.size) return null
            if (marker == MARKER_APP1 && bytes.startsWith(EXIF_HEADER, dataStart, segmentEnd.toInt())) {
                return dataStart + EXIF_HEADER.size to segmentEnd.toInt()
            }
            offset = segmentEnd.toInt()
        }
        return null
    }

    @Suppress("ReturnCount")
    private fun readTiffOrientation(
        bytes: ByteArray,
        start: Int,
        end: Int
    ): Int? {
        if (end - start < TIFF_HEADER_SIZE) return null
        val littleEndian = when (bytes.u16(start, littleEndian = false)) {
            BYTE_ORDER_LITTLE_ENDIAN -> true
            BYTE_ORDER_BIG_ENDIAN -> false
            else -> return null
        }
        if (bytes.u16(start + TIFF_MAGIC_POSITION, littleEndian) != TIFF_MAGIC) return null
        val ifdOffset = bytes.u32(start + TIFF_IFD_OFFSET_POSITION, littleEndian) ?: return null
        val ifdStart = start + ifdOffset
        if (ifdOffset < TIFF_HEADER_SIZE || ifdStart + IFD_COUNT_SIZE > end) return null

        val entryCount = bytes.u16(ifdStart.toInt(), littleEndian) ?: return null
        for (index in 0 until entryCount) {
            val entry = ifdStart + IFD_COUNT_SIZE + index.toLong() * IFD_ENTRY_SIZE
            if (entry + IFD_ENTRY_SIZE > end) return null
            val entryOffset = entry.toInt()
            if (bytes.u16(entryOffset, littleEndian) == TAG_ORIENTATION) {
                val type = bytes.u16(entryOffset + ENTRY_TYPE_POSITION, littleEndian)
                val count = bytes.u32(entryOffset + ENTRY_COUNT_POSITION, littleEndian)
                if (type != TYPE_SHORT || count != 1L) return null
                // A single SHORT is stored left-aligned in the 4 byte value field
                return bytes.u16(entryOffset + ENTRY_VALUE_POSITION, littleEndian)
            }
        }
        return null
    }

    private fun ByteArray.u8(index: Int): Int? = getOrNull(index)?.toInt()?.and(BYTE_MASK)

    private fun ByteArray.u16(index: Int, littleEndian: Boolean): Int? {
        val first = u8(index) ?: return null
        val second = u8(index + 1) ?: return null
        return if (littleEndian) {
            second shl BITS_PER_BYTE or first
        } else {
            first shl BITS_PER_BYTE or second
        }
    }

    /** Unsigned 32 bit value as a Long so that large offsets can not overflow into valid ones */
    private fun ByteArray.u32(index: Int, littleEndian: Boolean): Long? {
        val first = u16(index, littleEndian)?.toLong() ?: return null
        val second = u16(index + 2, littleEndian)?.toLong() ?: return null
        return if (littleEndian) {
            second shl 2 * BITS_PER_BYTE or first
        } else {
            first shl 2 * BITS_PER_BYTE or second
        }
    }

    private fun ByteArray.startsWith(
        prefix: ByteArray,
        from: Int,
        until: Int
    ): Boolean = until - from >= prefix.size && prefix.indices.all { this[from + it] == prefix[it] }
}
