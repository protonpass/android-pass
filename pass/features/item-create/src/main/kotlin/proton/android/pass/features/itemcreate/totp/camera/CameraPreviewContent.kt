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

import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import proton.android.pass.log.api.PassLogger
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private const val FOCUS_AUTO_CANCEL_SECONDS = 3L
private const val FOCUS_RETRY_DELAY_MILLIS = 1500L

@Composable
fun CameraPreviewContent(
    modifier: Modifier = Modifier,
    onOpenImagePicker: () -> Unit,
    onSuccess: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var previewViewSize by remember { mutableStateOf(Size.Zero) }
    var cutoutRect by remember { mutableStateOf(Rect.Zero) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var canScanCode by remember { mutableStateOf(true) }

    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val qrAnalysisExecutor = remember { Executors.newSingleThreadExecutor() }

    val preview = remember { Preview.Builder().build() }
    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }
    val processCameraProvider = remember(context) {
        ProcessCameraProvider.getInstance(context).get()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> canScanCode = true
                Lifecycle.Event.ON_PAUSE -> canScanCode = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        try {
            processCameraProvider.unbindAll()
            camera = processCameraProvider.bindToLifecycle(
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

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            imageAnalysis.clearAnalyzer()
            qrAnalysisExecutor.shutdown()
            camera = null
            previewView = null
            processCameraProvider.unbindAll()
        }
    }

    LaunchedEffect(previewViewSize) {
        if (previewViewSize == Size.Zero) return@LaunchedEffect

        val cutoutSize = previewViewSize.minDimension * QrCodeImageAnalyzer.SCAN_WINDOW_SIZE_RATIO
        val left = (previewViewSize.width - cutoutSize) / 2f
        val top = (previewViewSize.height - cutoutSize) / 3f
        cutoutRect = Rect(
            left = left,
            top = top,
            right = left + cutoutSize,
            bottom = top + cutoutSize
        )

        imageAnalysis.setAnalyzer(
            qrAnalysisExecutor,
            QrCodeImageAnalyzer(
                previewWidthProvider = { previewViewSize.width },
                previewHeightProvider = { previewViewSize.height },
                cutoutRectProvider = {
                    android.graphics.Rect(
                        cutoutRect.left.toInt(),
                        cutoutRect.top.toInt(),
                        cutoutRect.right.toInt(),
                        cutoutRect.bottom.toInt()
                    )
                },
                onSuccess = { value ->
                    mainExecutor.execute {
                        if (canScanCode) {
                            canScanCode = false
                            onSuccess(value)
                        }
                    }
                },
                onError = {}
            )
        )
    }

    LaunchedEffect(camera, previewView, cutoutRect, canScanCode) {
        val currentCamera = camera ?: return@LaunchedEffect
        val currentPreviewView = previewView ?: return@LaunchedEffect
        if (cutoutRect == Rect.Zero) return@LaunchedEffect

        while (canScanCode) {
            runCatching {
                val meteringPoint = currentPreviewView.meteringPointFactory
                    .createPoint(cutoutRect.center.x, cutoutRect.center.y)
                val focusAction = FocusMeteringAction.Builder(
                    meteringPoint,
                    FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                ).setAutoCancelDuration(FOCUS_AUTO_CANCEL_SECONDS, TimeUnit.SECONDS).build()
                currentCamera.cameraControl.startFocusAndMetering(focusAction)
            }
            delay(FOCUS_RETRY_DELAY_MILLIS)
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { factoryContext ->
                PreviewView(factoryContext).apply {
                    previewView = this
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    post {
                        previewViewSize = Size(width.toFloat(), height.toFloat())
                    }
                }
            },
            update = { view ->
                preview.setSurfaceProvider(view.surfaceProvider)
            }
        )
        CameraPreviewTopBar(onOpenImagePicker = onOpenImagePicker, onDismiss = onDismiss)
    }
}

private const val TAG = "CameraPreviewContent"
