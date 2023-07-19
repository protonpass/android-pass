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

package proton.android.pass.featureitemcreate.impl.totp

import androidx.compose.animation.ExperimentalAnimationApi
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

object PhotoPickerTotp : NavItem(
    baseRoute = "totp/photopicker",
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
        CameraPreviewTotp(
            onUriReceived = { uri -> onSuccess(uri, totpIndexField) },
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
