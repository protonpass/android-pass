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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
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
    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(
                    Dispatchers.Main.asExecutor(),
                    QrCodeImageAnalyzer(
                        onSuccess = onSuccess,
                        onError = {}
                    )
                )
            }
    }
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
