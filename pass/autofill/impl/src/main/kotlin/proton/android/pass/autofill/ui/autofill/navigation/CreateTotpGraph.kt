package proton.android.pass.autofill.ui.autofill.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import proton.android.pass.autofill.ui.autofill.AutofillNavItem
import proton.android.pass.featurecreateitem.impl.totp.CreateManualTotp
import proton.android.pass.featurecreateitem.impl.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.featurecreateitem.impl.totp.camera.CameraPreviewTotp
import proton.android.pass.featurecreateitem.impl.totp.photopicker.PhotoPickerTotp
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.navigation.api.composable

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.createTotpGraph(nav: AppNavigator) {
    composable(AutofillNavItem.CreateTotp) {
        CreateManualTotp(
            onAddManualTotp = { totp -> nav.navigateUpWithResult(TOTP_NAV_PARAMETER_KEY, totp) },
            onCloseManualTotp = { nav.onBackClick() }
        )
    }
    composable(AutofillNavItem.CameraTotp) {
        var uriFound: String? by remember { mutableStateOf(null) }
        uriFound?.let { uri ->
            LaunchedEffect(Unit) {
                nav.navigateUpWithResult(TOTP_NAV_PARAMETER_KEY, uri)
            }
        }
        CameraPreviewTotp(
            onUriReceived = { uri -> uriFound = uri },
            onClosePreview = { nav.onBackClick() }
        )
    }
    composable(AutofillNavItem.PhotoPickerTotp) {
        PhotoPickerTotp(
            onQrReceived = { uri -> nav.navigateUpWithResult(TOTP_NAV_PARAMETER_KEY, uri) },
            onQrNotDetected = { nav.onBackClick() },
            onPhotoPickerDismissed = { nav.onBackClick() }
        )
    }
}
