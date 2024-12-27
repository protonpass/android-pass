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

package proton.android.pass.features.itemcreate.totp.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.PlanarYUVLuminanceSource
import proton.android.pass.features.itemcreate.totp.imageprocessing.ZxingWrapper
import java.nio.ByteBuffer

class QrCodeImageAnalyzer(
    val onSuccess: (String) -> Unit,
    val onError: (Throwable) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        val source = PlanarYUVLuminanceSource(
            /* yuvData = */
            image.planes[0].buffer.toByteArray(),
            /* dataWidth = */
            image.width,
            /* dataHeight = */
            image.height,
            /* left = */
            0,
            /* top = */
            0,
            /* width = */
            image.width,
            /* height = */
            image.height,
            /* reverseHorizontal = */
            false
        )
        ZxingWrapper.tryReadingQrCode(source)
            .onSuccess { onSuccess(it) }
            .onFailure { onError(it) }
        image.close()
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }
}
