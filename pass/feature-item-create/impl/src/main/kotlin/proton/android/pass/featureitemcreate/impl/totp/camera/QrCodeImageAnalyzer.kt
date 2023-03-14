package proton.android.pass.featureitemcreate.impl.totp.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.PlanarYUVLuminanceSource
import proton.android.pass.featureitemcreate.impl.totp.imageprocessing.ZxingWrapper
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
