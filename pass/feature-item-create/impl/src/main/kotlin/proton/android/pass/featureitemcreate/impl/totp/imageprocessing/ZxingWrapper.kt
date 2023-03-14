package proton.android.pass.featureitemcreate.impl.totp.imageprocessing

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.common.HybridBinarizer

object ZxingWrapper {
    private val reader = MultiFormatReader()
        .apply {
            setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)))
        }

    fun tryReadingQrCode(source: LuminanceSource): Result<String> =
        try {
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            Result.success(reader.decodeWithState(binaryBitmap).text)
        } catch (e: NotFoundException) {
            Result.failure(e)
        }
}
