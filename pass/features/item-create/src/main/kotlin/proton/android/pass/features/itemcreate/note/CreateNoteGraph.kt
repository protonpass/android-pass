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

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.features.itemcreate.common.KEY_VAULT_SELECTED
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath
import java.net.URI

object CreateNote : NavItem(
    baseRoute = "note/create",
    optionalArgIds = listOf(CommonOptionalNavArgId.ShareId)
) {
    fun createNavRoute(shareId: Option<ShareId>) = buildString {
        append(baseRoute)
        val map = mutableMapOf<String, Any>()
        if (shareId is Some) {
            map[CommonOptionalNavArgId.ShareId.key] = shareId.value.id
        }
        val path = map.toPath()
        append(path)
    }
}

fun NavGraphBuilder.createNoteGraph(onNavigate: (CreateNoteNavigation) -> Unit) {
    composable(CreateNote) { navBackStack ->
        val selectVault by navBackStack.savedStateHandle
            .getStateFlow<String?>(KEY_VAULT_SELECTED, null)
            .collectAsStateWithLifecycle()

        CreateNoteScreen(
            selectVault = selectVault.toOption().map { ShareId(it) }.value(),
            onNavigate = onNavigate
        )
    }
}

sealed interface CreateNoteNavigation {
    data class SelectVault(val shareId: ShareId) : CreateNoteNavigation
    data object NoteCreated : CreateNoteNavigation
    data object AddAttachment : CreateNoteNavigation

    @JvmInline
    value class DeleteAllAttachments(val attachmentIds: Set<AttachmentId>) : CreateNoteNavigation
    data object Back : CreateNoteNavigation

    @JvmInline
    value class OpenDraftAttachmentOptions(val uri: URI) : CreateNoteNavigation

    data object UpsellAttachments : CreateNoteNavigation
}
