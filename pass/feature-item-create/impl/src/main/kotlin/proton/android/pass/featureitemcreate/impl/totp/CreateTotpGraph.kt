package proton.android.pass.featureitemcreate.impl.totp

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.featureitemcreate.impl.totp.camera.CameraPreviewTotp
import proton.android.pass.featureitemcreate.impl.totp.photopicker.PhotoPickerTotpScreen
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath

const val TOTP_NAV_PARAMETER_KEY = "totp_nav_parameter_key"
const val INDEX_NAV_PARAMETER_KEY = "index_nav_parameter_key"

object CameraTotp : NavItem(
    baseRoute = "totp/camera",
    optionalArgIds = listOf(TotpOptionalNavArgId.TotpIndexField)
) {
    fun createNavRoute(index: Option<Int> = None) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        map[TotpOptionalNavArgId.TotpIndexField.key] = index.value() ?: -1
        val path = map.toPath()
        append(path)
    }
}

enum class TotpOptionalNavArgId : OptionalNavArgId {
    TotpIndexField {
        override val key: String = "index"
        override val navType: NavType<*> = NavType.IntType
    },
}

object PhotoPickerTotp : NavItem(baseRoute = "totp/photopicker")

@OptIn(
    ExperimentalAnimationApi::class
)
fun NavGraphBuilder.createTotpGraph(
    onSuccess: (String, Int?) -> Unit,
    onCloseTotp: () -> Unit,
    onOpenImagePicker: (Int?) -> Unit
) {
    composable(CameraTotp) { backStackEntry ->
        val totpIndexField =
            backStackEntry.arguments?.getInt(TotpOptionalNavArgId.TotpIndexField.key)
        var uriFound: String? by remember { mutableStateOf(null) }
        uriFound?.let { uri ->
            LaunchedEffect(Unit) { onSuccess(uri, totpIndexField) }
        }
        CameraPreviewTotp(
            onUriReceived = { uri -> uriFound = uri },
            onOpenImagePicker = { onOpenImagePicker(totpIndexField) },
            onClosePreview = onCloseTotp
        )
    }
    composable(PhotoPickerTotp) { backStackEntry ->
        val totpIndexField =
            backStackEntry.arguments?.getInt(TotpOptionalNavArgId.TotpIndexField.key)
        PhotoPickerTotpScreen(
            onQrReceived = { uri -> onSuccess(uri, totpIndexField) },
            onQrNotDetected = onCloseTotp,
            onPhotoPickerDismissed = onCloseTotp
        )
    }
}
