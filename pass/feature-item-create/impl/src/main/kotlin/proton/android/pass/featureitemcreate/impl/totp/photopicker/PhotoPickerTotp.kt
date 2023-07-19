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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.zxing.RGBLuminanceSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import proton.android.pass.featureitemcreate.impl.totp.imageprocessing.ZxingWrapper
import proton.android.pass.log.api.PassLogger
import java.io.FileNotFoundException
import java.io.IOException

@Suppress("DEPRECATION")
@Composable
fun PhotoPickerTotpScreen(
    onQrReceived: (String) -> Unit,
    onQrNotDetected: () -> Unit,
    onPhotoPickerDismissed: () -> Unit
) {
    val context = LocalContext.current
    var totpUriResult by remember { mutableStateOf<TotpUriResult>(TotpUriResult.NotStarted) }
    val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) {
        totpUriResult = if (it != null) {
            TotpUriResult.Success(it)
        } else {
            TotpUriResult.Cancelled
        }
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(totpUriResult) {
        when (val result = totpUriResult) {
            TotpUriResult.Cancelled -> onPhotoPickerDismissed()
            TotpUriResult.NotStarted -> {}
            is TotpUriResult.Success -> {
                scope.launch(Dispatchers.Default) {
                    try {
                        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.decodeBitmap(
                                ImageDecoder.createSource(context.contentResolver, result.uri)
                            ).copy(Bitmap.Config.ARGB_8888, false)
                        } else {
                            MediaStore.Images.Media.getBitmap(context.contentResolver, result.uri)
                        }
                        val pixels = IntArray(bitmap.width * bitmap.height)
                        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                        val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
                        ZxingWrapper.tryReadingQrCode(source)
                            .onSuccess { withContext(Dispatchers.Main) { onQrReceived(it) } }
                            .onFailure { withContext(Dispatchers.Main) { onQrNotDetected() } }
                    } catch (e: IOException) {
                        PassLogger.w(TAG, e, "Error decoding bitmap")
                        withContext(Dispatchers.Main) { onQrNotDetected() }
                    } catch (e: FileNotFoundException) {
                        PassLogger.w(TAG, e, "File not found")
                        withContext(Dispatchers.Main) { onQrNotDetected() }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
    }
}

private const val TAG = "PhotoPickerTotp"
