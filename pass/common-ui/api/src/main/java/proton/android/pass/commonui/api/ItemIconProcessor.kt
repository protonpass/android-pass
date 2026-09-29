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

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.caverock.androidsvg.SVG
import proton.android.pass.common.api.ItemIcon
import proton.android.pass.common.api.ItemIconError
import proton.android.pass.common.api.ItemIconResult
import proton.android.pass.log.api.PassLogger
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

/**
 * Converts a picked image into an item icon data URI (see [ItemIcon.process]).
 * Performs blocking IO and decoding: call it off the main thread.
 */
object ItemIconProcessor {

    private const val TAG = "ItemIconProcessor"
    private const val PNG_QUALITY = 100

    fun process(
        contentResolver: ContentResolver,
        uri: Uri,
        mimeType: String? = resolveMimeType(contentResolver, uri)
    ): ItemIconResult {
        if (!ItemIcon.isAcceptedMimeType(mimeType)) return ItemIconResult.Error(ItemIconError.Type)

        val bytes = readBytes(contentResolver, uri) ?: return ItemIconResult.Error(ItemIconError.Decode)

        return ItemIcon.process(mimeType, bytes, ::isRenderableSvg, ::rasterize)
    }

    /**
     * The provider's type can be missing, generic (`application/octet-stream`) or non-standard
     * (`image/jpg`): fall back to the extension of the file's display name.
     */
    fun resolveMimeType(contentResolver: ContentResolver, uri: Uri): String? {
        val providerType = ItemIcon.normalizeMimeType(safeCall { contentResolver.getType(uri) })
        if (ItemIcon.isAcceptedMimeType(providerType)) return providerType

        val extension = (displayName(contentResolver, uri) ?: uri.lastPathSegment)
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase()
            ?.takeIf { it.isNotEmpty() }
            ?: return providerType
        val extensionType = ItemIcon.normalizeMimeType(
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        )
        return if (ItemIcon.isAcceptedMimeType(extensionType)) extensionType else providerType
    }

    /**
     * Structural check: the SVG renderer must be able to parse the document. Reading the
     * document size throws if the document has no root `<svg>` element.
     */
    @Suppress("TooGenericExceptionCaught")
    fun isRenderableSvg(bytes: ByteArray): Boolean = try {
        SVG.getFromInputStream(ByteArrayInputStream(bytes)).documentWidth
        true
    } catch (e: Exception) {
        PassLogger.w(TAG, e)
        false
    }

    /** Center-crops and scales the image to [ItemIcon.SIZE_PX] and exports it as PNG */
    fun rasterize(bytes: ByteArray): ByteArray? {
        val source = decodeBitmap(bytes) ?: return null
        val width = source.width
        val height = source.height
        if (width <= 0 || height <= 0) return null

        val side = minOf(width, height)
        val left = (width - side) / 2
        val top = (height - side) / 2

        val output = Bitmap.createBitmap(ItemIcon.SIZE_PX, ItemIcon.SIZE_PX, Bitmap.Config.ARGB_8888)
        Canvas(output).drawBitmap(
            source,
            Rect(left, top, left + side, top + side),
            Rect(0, 0, ItemIcon.SIZE_PX, ItemIcon.SIZE_PX),
            Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        )
        source.recycle()

        return ByteArrayOutputStream().use { stream ->
            val compressed = output.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream)
            output.recycle()
            if (compressed) stream.toByteArray() else null
        }
    }

    /** Reads one byte more than accepted so that oversized files are detected without reading them fully */
    private fun readBytes(contentResolver: ContentResolver, uri: Uri): ByteArray? = try {
        contentResolver.openInputStream(uri)?.use { it.readAtMost(ItemIcon.MAX_INPUT_SIZE + 1) }
    } catch (e: IOException) {
        PassLogger.w(TAG, e)
        null
    } catch (e: SecurityException) {
        PassLogger.w(TAG, e)
        null
    }

    private fun displayName(contentResolver: ContentResolver, uri: Uri): String? = safeCall {
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun <T> safeCall(block: () -> T?): T? = try {
        block()
    } catch (e: Exception) {
        PassLogger.w(TAG, e)
        null
    }

    @Suppress("TooGenericExceptionCaught")
    private fun decodeBitmap(bytes: ByteArray): Bitmap? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(ByteBuffer.wrap(bytes))) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.setTargetSampleSize(sampleSize(info.size.width, info.size.height))
            }
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight)
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        }
    } catch (e: Exception) {
        PassLogger.w(TAG, e)
        null
    }

    /** Largest power of two that keeps the shortest side at or above [ItemIcon.SIZE_PX] */
    private fun sampleSize(width: Int, height: Int): Int {
        var sampleSize = 1
        val side = minOf(width, height)
        while (side / (sampleSize * 2) >= ItemIcon.SIZE_PX) {
            sampleSize *= 2
        }
        return sampleSize
    }

    private fun InputStream.readAtMost(limit: Int): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0
        while (total < limit) {
            val read = read(buffer, 0, minOf(buffer.size, limit - total))
            if (read < 0) break
            output.write(buffer, 0, read)
            total += read
        }
        return output.toByteArray()
    }
}
