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
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.features.attachments.addattachment.navigation.AddAttachmentNavItem
import proton.android.pass.features.attachments.addattachment.navigation.AddAttachmentNavigation
import proton.android.pass.features.attachments.addattachment.ui.AddAttachmentBottomsheet
import proton.android.pass.features.attachments.attachmentoptionsondetail.navigation.AttachmentOptionsOnDetailNavItem
import proton.android.pass.features.attachments.attachmentoptionsondetail.navigation.AttachmentOptionsOnDetailNavigation
import proton.android.pass.features.attachments.attachmentoptionsondetail.ui.AttachmentOptionsOnDetailBottomsheet
import proton.android.pass.features.attachments.attachmentoptionsonedit.navigation.AttachmentOptionsOnEditNavItem
import proton.android.pass.features.attachments.attachmentoptionsonedit.navigation.AttachmentOptionsOnEditNavigation
import proton.android.pass.features.attachments.attachmentoptionsonedit.ui.AttachmentOptionsOnEditBottomsheet
import proton.android.pass.features.attachments.camera.navigation.CameraNavItem
import proton.android.pass.features.attachments.camera.navigation.CameraNavigation
import proton.android.pass.features.attachments.camera.ui.CameraScreen
import proton.android.pass.features.attachments.deleteall.navigation.DeleteAllAttachmentsDialogNavItem
import proton.android.pass.features.attachments.deleteall.navigation.DeleteAllAttachmentsNavigation
import proton.android.pass.features.attachments.deleteall.ui.DeleteAllAttachmentsDialog
import proton.android.pass.features.attachments.filepicker.navigation.FilePickerNavItem
import proton.android.pass.features.attachments.filepicker.navigation.FilePickerNavigation
import proton.android.pass.features.attachments.filepicker.ui.FilePickerScreen
import proton.android.pass.features.attachments.mediapicker.navigation.MediaPickerNavItem
import proton.android.pass.features.attachments.mediapicker.navigation.MediaPickerNavigation
import proton.android.pass.features.attachments.mediapicker.ui.MediaPickerScreen
import proton.android.pass.features.attachments.renameattachment.navigation.RenameAttachmentNavItem
import proton.android.pass.features.attachments.renameattachment.navigation.RenameAttachmentNavigation
import proton.android.pass.features.attachments.renameattachment.ui.RenameAttachmentDialog
import proton.android.pass.features.attachments.storagefull.navigation.StorageFullNavItem
import proton.android.pass.features.attachments.storagefull.navigation.StorageFullNavigation
import proton.android.pass.features.attachments.storagefull.ui.StorageFullBottomsheet
import proton.android.pass.navigation.api.bottomSheet
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.dialog
import java.net.URI

@Suppress("LongMethod")
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
    bottomSheet(navItem = AttachmentOptionsOnEditNavItem) {
        AttachmentOptionsOnEditBottomsheet(
            onNavigate = {
                when (it) {
                    AttachmentOptionsOnEditNavigation.CloseBottomsheet ->
                        onNavigate(AttachmentsNavigation.CloseBottomsheet)

                    is AttachmentOptionsOnEditNavigation.OpenRenameAttachment ->
                        onNavigate(
                            AttachmentsNavigation.OpenRenameAttachment(
                                shareId = it.shareId,
                                itemId = it.itemId,
                                attachmentId = it.attachmentId
                            )
                        )

                    is AttachmentOptionsOnEditNavigation.OpenRenameDraftAttachment ->
                        onNavigate(AttachmentsNavigation.OpenRenameDraftAttachment(it.uri))
                }
            }
        )
    }
    bottomSheet(navItem = AttachmentOptionsOnDetailNavItem) {
        AttachmentOptionsOnDetailBottomsheet(
            onNavigate = {
                when (it) {
                    AttachmentOptionsOnDetailNavigation.CloseBottomsheet ->
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
    dialog(DeleteAllAttachmentsDialogNavItem) {
        DeleteAllAttachmentsDialog(
            onNavigate = {
                when (it) {
                    DeleteAllAttachmentsNavigation.CloseDialog -> onNavigate(AttachmentsNavigation.CloseScreen)
                }
            }
        )
    }
    dialog(RenameAttachmentNavItem) {
        RenameAttachmentDialog {
            when (it) {
                RenameAttachmentNavigation.CloseDialog -> onNavigate(AttachmentsNavigation.CloseScreen)
            }
        }
    }
    bottomSheet(navItem = StorageFullNavItem) {
        StorageFullBottomsheet(
            onNavigate = {
                when (it) {
                    StorageFullNavigation.Upgrade -> onNavigate(AttachmentsNavigation.Upgrade)
                    StorageFullNavigation.CloseBottomsheet -> onNavigate(AttachmentsNavigation.CloseBottomsheet)
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
    data object Upgrade : AttachmentsNavigation

    data class OpenRenameAttachment(
        val shareId: ShareId,
        val itemId: ItemId,
        val attachmentId: AttachmentId
    ) : AttachmentsNavigation

    @JvmInline
    value class OpenRenameDraftAttachment(val uri: URI) : AttachmentsNavigation
}
