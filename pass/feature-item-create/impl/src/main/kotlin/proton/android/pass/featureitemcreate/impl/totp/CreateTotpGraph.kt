package proton.android.pass.featureitemcreate.impl.totp

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import proton.android.pass.featureitemcreate.impl.totp.camera.CameraPreviewTotp
import proton.android.pass.featureitemcreate.impl.totp.photopicker.PhotoPickerTotpScreen
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object CreateTotp : NavItem(baseRoute = "totp/create")
object CameraTotp : NavItem(baseRoute = "totp/camera")
object PhotoPickerTotp : NavItem(baseRoute = "totp/photopicker")

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.createTotpGraph(
    onUriReceived: (String) -> Unit,
    onCloseTotp: () -> Unit,
    onOpenImagePicker: () -> Unit
) {
    composable(CreateTotp) {
        CreateManualTotp(
            onAddManualTotp = onUriReceived,
            onCloseManualTotp = onCloseTotp
        )
    }
    composable(CameraTotp) {
        var uriFound: String? by remember { mutableStateOf(null) }
        uriFound?.let { uri ->
            LaunchedEffect(Unit) { onUriReceived(uri) }
        }
        CameraPreviewTotp(
            onUriReceived = { uri -> uriFound = uri },
            onOpenImagePicker = onOpenImagePicker,
            onClosePreview = onCloseTotp
        )
    }
    composable(PhotoPickerTotp) {
        PhotoPickerTotpScreen(
            onQrReceived = onUriReceived,
            onQrNotDetected = onCloseTotp,
            onPhotoPickerDismissed = onCloseTotp
        )
    }
}
