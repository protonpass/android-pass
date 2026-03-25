/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.features.itemcreate.totp.imageprocessing

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.ReaderException
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer

object ZxingWrapper {
    private val hints = mapOf(
        DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
        DecodeHintType.TRY_HARDER to true,
        DecodeHintType.ALSO_INVERTED to true
    )

    private val reader = MultiFormatReader().apply { setHints(hints) }

    fun tryReadingQrCode(source: LuminanceSource): Result<String> {
        // First pass: HybridBinarizer (better for real photos)
        try {
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            return Result.success(reader.decodeWithState(bitmap).text)
        } catch (_: ReaderException) {
        } finally {
            reader.reset()
        }
        // Second pass: GlobalHistogramBinarizer (better for screens / low contrast)
        return try {
            val bitmap = BinaryBitmap(GlobalHistogramBinarizer(source))
            Result.success(reader.decodeWithState(bitmap).text)
        } catch (e: ReaderException) {
            Result.failure(e)
        } finally {
            reader.reset()
        }
    }
}
