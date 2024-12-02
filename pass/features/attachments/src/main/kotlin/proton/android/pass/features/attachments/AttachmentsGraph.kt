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
import proton.android.pass.navigation.api.bottomSheet

fun NavGraphBuilder.attachmentsGraph(onNavigate: (AttachmentsNavigation) -> Unit) {
    bottomSheet(navItem = AddAttachmentNavItem) {
        AddAttachmentBottomsheet(
            onNavigate = {
                when (it) {
                    AddAttachmentNavigation.CloseBottomsheet ->
                        onNavigate(AttachmentsNavigation.CloseBottomsheet)
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
}

sealed interface AttachmentsNavigation {
    data object CloseBottomsheet : AttachmentsNavigation
}
