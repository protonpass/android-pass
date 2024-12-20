/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.attachments

import androidx.navigation.NavGraphBuilder
import proton.android.pass.features.attachments.addattachment.navigation.AddAttachmentNavItem
import proton.android.pass.features.attachments.addattachment.navigation.AddAttachmentNavigation
import proton.android.pass.features.attachments.addattachment.ui.AddAttachmentBottomsheet
import proton.android.pass.features.attachments.attachmentoptions.navigation.AttachmentOptionsNavItem
import proton.android.pass.features.attachments.attachmentoptions.navigation.AttachmentOptionsNavigation
import proton.android.pass.features.attachments.attachmentoptions.ui.AttachmentOptionsBottomsheet
import proton.android.pass.features.attachments.camera.navigation.CameraNavItem
import proton.android.pass.features.attachments.camera.navigation.CameraNavigation
import proton.android.pass.features.attachments.camera.ui.CameraScreen
import proton.android.pass.features.attachments.filepicker.navigation.FilePickerNavItem
import proton.android.pass.features.attachments.filepicker.navigation.FilePickerNavigation
import proton.android.pass.features.attachments.filepicker.ui.FilePickerScreen
import proton.android.pass.features.attachments.mediapicker.navigation.MediaPickerNavItem
import proton.android.pass.features.attachments.mediapicker.navigation.MediaPickerNavigation
import proton.android.pass.features.attachments.mediapicker.ui.MediaPickerScreen
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable

fun NavGraphBuilder.attachmentsGraph(onNavigate: (AttachmentsNavigation) -> Unit) {
    bottomSheet(navItem = AddAttachmentNavItem) {
        AddAttachmentBottomsheet(
            onNavigate = {
                when (it) {
                    AddAttachmentNavigation.CloseBottomsheet ->
                        onNavigate(AttachmentsNavigation.CloseBottomsheet)

                    AddAttachmentNavigation.OpenFilePicker ->
                        onNavigate(AttachmentsNavigation.OpenFilePicker)

                    AddAttachmentNavigation.OpenMediaPicker ->
                        onNavigate(AttachmentsNavigation.OpenMediaPicker)

                    AddAttachmentNavigation.OpenCamera ->
                        onNavigate(AttachmentsNavigation.OpenCamera)
                }
            }
        )
    }
    bottomSheet(navItem = AttachmentOptionsNavItem) {
        AttachmentOptionsBottomsheet(
            onNavigate = {
                when (it) {
                    AttachmentOptionsNavigation.CloseBottomsheet ->
                        onNavigate(AttachmentsNavigation.CloseBottomsheet)
                }
            }
        )
    }
    composable(FilePickerNavItem) {
        FilePickerScreen(
            onNavigate = {
                when (it) {
                    FilePickerNavigation.Close -> onNavigate(AttachmentsNavigation.CloseScreen)
                }
            }
        )
    }
    composable(MediaPickerNavItem) {
        MediaPickerScreen(
            onNavigate = {
                when (it) {
                    MediaPickerNavigation.Close -> onNavigate(AttachmentsNavigation.CloseScreen)
                }
            }
        )
    }
    composable(CameraNavItem) {
        CameraScreen(
            onNavigate = {
                when (it) {
                    CameraNavigation.Close -> onNavigate(AttachmentsNavigation.CloseScreen)
                }
            }
        )
    }
}

sealed interface AttachmentsNavigation {
    data object CloseBottomsheet : AttachmentsNavigation
    data object CloseScreen : AttachmentsNavigation
    data object OpenFilePicker : AttachmentsNavigation
    data object OpenMediaPicker : AttachmentsNavigation
    data object OpenCamera : AttachmentsNavigation
}
