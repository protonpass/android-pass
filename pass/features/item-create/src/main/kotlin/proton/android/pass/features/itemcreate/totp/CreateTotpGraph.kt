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

package proton.android.pass.features.itemcreate.totp

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.totp.camera.CameraPreviewTotp
import proton.android.pass.features.itemcreate.totp.photopicker.PhotoPickerTotpScreen
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.OptionalNavArgId
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath

const val TOTP_NAV_PARAMETER_KEY = "totp_nav_parameter_key"
const val SECTION_INDEX_NAV_PARAMETER_KEY = "section_index_nav_parameter_key"
const val INDEX_NAV_PARAMETER_KEY = "index_nav_parameter_key"

data class CameraTotpNavItem(val prefix: CustomFieldPrefix) : NavItem(
    baseRoute = "$prefix/totp/camera",
    optionalArgIds = listOf(
        TotpOptionalNavArgId.TotpSectionIndexField,
        TotpOptionalNavArgId.TotpIndexField
    )
) {
    fun createNavRoute(sectionIndex: Option<Int>, index: Option<Int>) = buildString {
        append(baseRoute)
        val path = mapOf(
            TotpOptionalNavArgId.TotpSectionIndexField.key to (sectionIndex.value() ?: -1),
            TotpOptionalNavArgId.TotpIndexField.key to (index.value() ?: -1)
        ).toPath()
        append(path)
    }

    companion object {
        val CreateLogin = CameraTotpNavItem(CustomFieldPrefix.CreateLogin)
        val CreateAlias = CameraTotpNavItem(CustomFieldPrefix.CreateAlias)
        val CreateIdentity = CameraTotpNavItem(CustomFieldPrefix.CreateIdentity)
    }
}

data class PhotoPickerTotpNavItem(val prefix: CustomFieldPrefix) : NavItem(
    baseRoute = "$prefix/totp/photopicker",
    optionalArgIds = listOf(
        TotpOptionalNavArgId.TotpSectionIndexField,
        TotpOptionalNavArgId.TotpIndexField
    )
) {
    fun createNavRoute(sectionIndex: Option<Int> = None, index: Option<Int>) = buildString {
        append(baseRoute)
        val path = mapOf(
            TotpOptionalNavArgId.TotpSectionIndexField.key to (sectionIndex.value() ?: -1),
            TotpOptionalNavArgId.TotpIndexField.key to (index.value() ?: -1)
        ).toPath()
        append(path)
    }

    companion object {
        val CreateLogin = PhotoPickerTotpNavItem(CustomFieldPrefix.CreateLogin)
        val CreateAlias = PhotoPickerTotpNavItem(CustomFieldPrefix.CreateAlias)
        val CreateIdentity = PhotoPickerTotpNavItem(CustomFieldPrefix.CreateIdentity)
    }
}

enum class TotpOptionalNavArgId : OptionalNavArgId {
    TotpSectionIndexField {
        override val key: String = "sectionIndex"
        override val navType: NavType<*> = NavType.IntType
    },
    TotpIndexField {
        override val key: String = "index"
        override val navType: NavType<*> = NavType.IntType
    }
}

fun NavGraphBuilder.createTotpGraph(
    prefix: CustomFieldPrefix,
    onSuccess: (String, Int?, Int?) -> Unit,
    onCloseTotp: () -> Unit,
    onOpenImagePicker: (Int?, Int?) -> Unit
) {
    composable(CameraTotpNavItem(prefix)) { backStackEntry ->
        val totpSectionIndexField =
            backStackEntry.arguments?.getInt(TotpOptionalNavArgId.TotpSectionIndexField.key)
                .takeIf { a: Int? -> a != null && a >= 0 }
        val totpIndexField =
            backStackEntry.arguments?.getInt(TotpOptionalNavArgId.TotpIndexField.key)
                .takeIf { a: Int? -> a != null && a >= 0 }
        CameraPreviewTotp(
            onUriReceived = { uri ->
                onSuccess(uri, totpSectionIndexField, totpIndexField)
            },
            onOpenImagePicker = { onOpenImagePicker(totpSectionIndexField, totpIndexField) },
            onClosePreview = onCloseTotp
        )
    }
    composable(PhotoPickerTotpNavItem(prefix)) { backStackEntry ->
        val totpSectionIndexField =
            backStackEntry.arguments?.getInt(TotpOptionalNavArgId.TotpSectionIndexField.key)
                .takeIf { a: Int? -> a != null && a >= 0 }
        val totpIndexField =
            backStackEntry.arguments?.getInt(TotpOptionalNavArgId.TotpIndexField.key)
                .takeIf { a: Int? -> a != null && a >= 0 }
        PhotoPickerTotpScreen(
            onQrReceived = { uri ->
                onSuccess(uri, totpSectionIndexField, totpIndexField)
            },
            onQrNotDetected = onCloseTotp,
            onPhotoPickerDismissed = onCloseTotp
        )
    }
}
