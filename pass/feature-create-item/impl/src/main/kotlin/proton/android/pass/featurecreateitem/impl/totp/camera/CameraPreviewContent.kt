package proton.android.pass.featurecreateitem.impl.totp.camera

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

@Composable
fun CameraPreviewContent(modifier: Modifier = Modifier, onSuccess: (String) -> Unit) {
    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(
                    Dispatchers.Main.asExecutor(),
                    QrCodeImageAnalyzer(
                        onSuccess = { result -> onSuccess(result.text) },
                        onError = {}
                    )
                )
            }
    }
    val context = LocalContext.current
    val processCameraProvider = remember(context) {
        ProcessCameraProvider.getInstance(context).get()
    }
    val preview = remember { Preview.Builder().build() }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        processCameraProvider.unbindAll()
        processCameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageAnalysis
        )
        onDispose { processCameraProvider.unbindAll() }
    }
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
    }
}
