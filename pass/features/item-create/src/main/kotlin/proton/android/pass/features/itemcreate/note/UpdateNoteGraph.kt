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

package proton.android.pass.features.itemcreate.note

import androidx.navigation.NavGraphBuilder
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldNavigation
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import java.net.URI

object EditNote : NavItem(
    baseRoute = "note/edit",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) = "$baseRoute/${shareId.id}/${itemId.id}"
}

fun NavGraphBuilder.updateNoteGraph(onNavigate: (UpdateNoteNavigation) -> Unit) {
    composable(EditNote) {
        UpdateNote(onNavigate = onNavigate)
    }
}

sealed interface UpdateNoteNavigation {
    data class NoteUpdated(val shareId: ShareId, val itemId: ItemId) : UpdateNoteNavigation
    data object CloseScreen : UpdateNoteNavigation
    data object AddAttachment : UpdateNoteNavigation
    data object UpsellAttachments : UpdateNoteNavigation
    data object Upgrade : UpdateNoteNavigation

    @JvmInline
    value class DeleteAllAttachments(val attachmentIds: Set<AttachmentId>) : UpdateNoteNavigation

    data class OpenAttachmentOptions(
        val shareId: ShareId,
        val itemId: ItemId,
        val attachmentId: AttachmentId
    ) : UpdateNoteNavigation

    @JvmInline
    value class OpenDraftAttachmentOptions(val uri: URI) : UpdateNoteNavigation

    @JvmInline
    value class NoteCustomFieldNavigation(val event: CustomFieldNavigation) : UpdateNoteNavigation
}
