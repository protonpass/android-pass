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

package proton.android.pass.featureitemcreate.impl.totp.camera

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import proton.android.pass.commonui.api.LifecycleEffect
import proton.android.pass.featureitemcreate.impl.totp.photopicker.TotpUriResult
import proton.android.pass.log.api.PassLogger

@Composable
fun CameraPreviewContent(
    modifier: Modifier = Modifier,
    onOpenImagePicker: () -> Unit,
    onSuccess: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val preview = remember { Preview.Builder().build() }
    CameraPreviewBindingDisposableEffect(preview, onSuccess)
    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                PreviewView(context)
                    .apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
            },
            update = { previewView ->
                preview.setSurfaceProvider(previewView.surfaceProvider)
            }
        )
        CameraPreviewTopBar(onOpenImagePicker = onOpenImagePicker, onDismiss = onDismiss)
    }
}

@Composable
private fun CameraPreviewBindingDisposableEffect(
    preview: Preview,
    onSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val processCameraProvider = remember(context) {
        ProcessCameraProvider.getInstance(context).get()
    }
    var totpUriResult by remember { mutableStateOf<TotpUriResult>(TotpUriResult.NotStarted) }
    val qrCodeImageAnalyzer = QrCodeImageAnalyzer(
        onSuccess = { totpUriResult = TotpUriResult.Success(it.toUri()) },
        onError = {}
    )
    val imageAnalysis = remember {
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(Dispatchers.Main.asExecutor(), qrCodeImageAnalyzer)
        imageAnalysis
    }
    LifecycleEffect(
        onResume = {
            when (val result = totpUriResult) {
                is TotpUriResult.Success -> onSuccess(result.uri.toString())
                else -> {}
            }
        }
    )
    DisposableEffect(lifecycleOwner) {
        try {
            processCameraProvider.unbindAll()
            processCameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
        } catch (e: IllegalStateException) {
            PassLogger.e(TAG, e)
        } catch (e: IllegalArgumentException) {
            PassLogger.e(TAG, e, "Cannot resolve camera")
        }
        onDispose { processCameraProvider.unbindAll() }
    }
}

private const val TAG = "CameraPreviewBindingDisposableEffect"
