package proton.android.pass.featureitemcreate.impl.totp.photopicker

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.zxing.RGBLuminanceSource
import proton.android.pass.featureitemcreate.impl.totp.imageprocessing.ZxingWrapper
import proton.android.pass.log.api.PassLogger
import java.io.FileNotFoundException
import java.io.IOException

@Composable
fun PhotoPickerTotpScreen(
    onQrReceived: (String) -> Unit,
    onQrNotDetected: () -> Unit,
    onPhotoPickerDismissed: () -> Unit
) {
    val context = LocalContext.current
    val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(context.contentResolver, uri)
                    ).copy(Bitmap.Config.ARGB_8888, false)
                } else {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                val pixels = IntArray(bitmap.width * bitmap.height)
                bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
                ZxingWrapper.tryReadingQrCode(source)
                    .onSuccess { onQrReceived(it) }
                    .onFailure { onQrNotDetected() }
            } catch (e: IOException) {
                PassLogger.w(TAG, e, "Error decoding bitmap")
                onQrNotDetected()
            } catch (e: FileNotFoundException) {
                PassLogger.w(TAG, e, "File not found")
                onQrNotDetected()
            }
        } else {
            onPhotoPickerDismissed()
        }
    }

    LaunchedEffect(Unit) {
        pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
    }
}

private const val TAG = "PhotoPickerTotp"
