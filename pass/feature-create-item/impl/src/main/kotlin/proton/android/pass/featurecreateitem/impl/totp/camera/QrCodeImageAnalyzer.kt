package proton.android.pass.featurecreateitem.impl.totp.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class QrCodeImageAnalyzer(
    val onSuccess: (Result) -> Unit,
    val onError: (Exception) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        setHints(
            mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE))
        )
    }

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
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            val result: Result = reader.decodeWithState(bitmap)
            onSuccess(result)
        } catch (e: NotFoundException) {
            onError(e)
        }
        image.close()
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }
}
