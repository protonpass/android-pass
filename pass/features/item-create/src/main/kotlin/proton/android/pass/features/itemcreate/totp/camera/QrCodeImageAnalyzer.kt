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

package proton.android.pass.features.itemcreate.totp.camera

import android.graphics.ImageFormat
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.PlanarYUVLuminanceSource
import proton.android.pass.features.itemcreate.totp.imageprocessing.ZxingWrapper
import java.nio.ByteBuffer

class QrCodeImageAnalyzer(
    private val previewWidthProvider: () -> Float = { 0f },
    private val previewHeightProvider: () -> Float = { 0f },
    private val cutoutRectProvider: () -> Rect = { Rect() },
    val onSuccess: (String) -> Unit,
    val onError: (Throwable) -> Unit
) : ImageAnalysis.Analyzer {

    private val supportedFormats = setOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888
    )

    override fun analyze(image: ImageProxy) {
        if (image.format !in supportedFormats) {
            image.close()
            return
        }
        try {
            val source = buildLuminanceSource(image)
            ZxingWrapper.tryReadingQrCode(source)
                .onSuccess { onSuccess(it) }
                .onFailure { onError(it) }
        } finally {
            image.close()
        }
    }

    private fun buildLuminanceSource(image: ImageProxy): PlanarYUVLuminanceSource {
        val yPlane = image.planes[0]
        val sourceWidth = image.width
        val sourceHeight = image.height
        val cutout = cutoutRectProvider()
        val crop = mapPreviewRectToSourceCrop(
            PreviewSourceCropInput(
                previewWidth = previewWidthProvider(),
                previewHeight = previewHeightProvider(),
                cutout = FloatCropRect(
                    left = cutout.left.toFloat(),
                    top = cutout.top.toFloat(),
                    right = cutout.right.toFloat(),
                    bottom = cutout.bottom.toFloat()
                ),
                rotationDegrees = image.imageInfo.rotationDegrees,
                sourceWidth = sourceWidth,
                sourceHeight = sourceHeight
            )
        )
        return PlanarYUVLuminanceSource(
            yPlane.buffer.toByteArray(),
            yPlane.rowStride,
            sourceHeight,
            crop.left,
            crop.top,
            crop.width(),
            crop.height(),
            false
        )
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        return ByteArray(remaining()).also(::get)
    }

    companion object {
        internal const val SCAN_WINDOW_SIZE_RATIO = 0.7f
        private const val FULL_ROTATION = 360
        private const val RIGHT_ANGLE = 90
        private const val UPSIDE_DOWN = 180
        private const val THREE_QUARTER_TURN = 270

        internal fun mapPreviewRectToSourceCrop(input: PreviewSourceCropInput): CropRect {
            if (input.previewWidth <= 0f || input.previewHeight <= 0f) {
                return CropRect(0, 0, input.sourceWidth, input.sourceHeight)
            }

            val rotation = (input.rotationDegrees % FULL_ROTATION + FULL_ROTATION) % FULL_ROTATION
            val isQuarterTurn = rotation == RIGHT_ANGLE || rotation == THREE_QUARTER_TURN
            val displayW = if (isQuarterTurn) input.sourceHeight else input.sourceWidth
            val displayH = if (isQuarterTurn) input.sourceWidth else input.sourceHeight

            val scale = maxOf(input.previewWidth / displayW, input.previewHeight / displayH)
            val offsetX = (displayW * scale - input.previewWidth) / 2f
            val offsetY = (displayH * scale - input.previewHeight) / 2f

            val displayCrop = CropRect(
                left = ((input.cutout.left + offsetX) / scale).toInt().coerceIn(0, displayW),
                top = ((input.cutout.top + offsetY) / scale).toInt().coerceIn(0, displayH),
                right = ((input.cutout.right + offsetX) / scale).toInt().coerceIn(1, displayW),
                bottom = ((input.cutout.bottom + offsetY) / scale).toInt().coerceIn(1, displayH)
            ).normalized()

            return when (rotation) {
                RIGHT_ANGLE -> CropRect(
                    left = displayCrop.top,
                    top = input.sourceHeight - displayCrop.right,
                    right = displayCrop.bottom,
                    bottom = input.sourceHeight - displayCrop.left
                )
                UPSIDE_DOWN -> CropRect(
                    left = input.sourceWidth - displayCrop.right,
                    top = input.sourceHeight - displayCrop.bottom,
                    right = input.sourceWidth - displayCrop.left,
                    bottom = input.sourceHeight - displayCrop.top
                )
                THREE_QUARTER_TURN -> CropRect(
                    left = input.sourceWidth - displayCrop.bottom,
                    top = displayCrop.left,
                    right = input.sourceWidth - displayCrop.top,
                    bottom = displayCrop.right
                )
                else -> displayCrop
            }.normalized()
        }
    }
}

internal data class CropRect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    fun width(): Int = right - left
    fun height(): Int = bottom - top
    fun normalized(): CropRect = CropRect(
        left = minOf(left, right),
        top = minOf(top, bottom),
        right = maxOf(left, right),
        bottom = maxOf(top, bottom)
    )
}

internal data class FloatCropRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

internal data class PreviewSourceCropInput(
    val previewWidth: Float,
    val previewHeight: Float,
    val cutout: FloatCropRect,
    val rotationDegrees: Int,
    val sourceWidth: Int,
    val sourceHeight: Int
)
