package proton.android.pass.featurecreateitem.impl.totp.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewTotp(modifier: Modifier = Modifier, onUriReceived: (String) -> Unit) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    if (cameraPermissionState.status.isGranted) {
        CameraPreviewContent(modifier, onUriReceived)
    } else {
        CameraPermissionContent(modifier, cameraPermissionState)
    }
}
