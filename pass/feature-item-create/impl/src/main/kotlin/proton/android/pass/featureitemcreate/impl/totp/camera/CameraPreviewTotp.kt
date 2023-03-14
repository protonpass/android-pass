package proton.android.pass.featureitemcreate.impl.totp.camera

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import proton.android.pass.log.api.PassLogger

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewTotp(
    modifier: Modifier = Modifier,
    onUriReceived: (String) -> Unit,
    onOpenImagePicker: () -> Unit,
    onClosePreview: () -> Unit
) {
    val cameraPermissionState: PermissionState =
        rememberPermissionState(android.Manifest.permission.CAMERA)
    if (cameraPermissionState.status.isGranted) {
        CameraPreviewContent(
            modifier = modifier,
            onOpenImagePicker = onOpenImagePicker,
            onSuccess = onUriReceived,
            onDismiss = onClosePreview
        )
    } else {
        val activity = LocalContext.current as Activity
        CameraPermissionContent(
            modifier = modifier,
            onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
            onOpenAppSettings = {
                try {
                    activity.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", activity.packageName, null)
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    PassLogger.d(TAG, e, "Settings not found")
                }
            },
            onDismiss = onClosePreview
        )
    }
}

private const val TAG = "CameraPreviewTotp"
