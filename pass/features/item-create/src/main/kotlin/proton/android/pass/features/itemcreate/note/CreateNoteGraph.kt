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
import proton.android.pass.features.itemcreate.bottomsheets.customfield.customFieldBottomSheetGraph
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.common.KEY_VAULT_SELECTED
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldNavigation
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameNavigation
import proton.android.pass.features.itemcreate.dialogs.customfield.customFieldNameDialogGraph
import proton.android.pass.features.itemcreate.note.CreateNoteNavigation.NoteCustomFieldNavigation
import proton.android.pass.features.itemcreate.totp.INDEX_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.createTotpGraph
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.toPath
import java.net.URI

object CreateNoteNavItem : NavItem(
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
    composable(CreateNoteNavItem) { navBackStack ->
        val selectVault by navBackStack.savedStateHandle
            .getStateFlow<String?>(KEY_VAULT_SELECTED, null)
            .collectAsStateWithLifecycle()

        CreateNoteScreen(
            selectVault = selectVault.toOption().map { ShareId(it) }.value(),
            onNavigate = onNavigate
        )
    }
    customFieldBottomSheetGraph(
        prefix = CustomFieldPrefix.CreateNote,
        onAddCustomFieldNavigate = { type, _ ->
            val event =
                NoteCustomFieldNavigation(CustomFieldNavigation.CustomFieldTypeSelected(type))
            onNavigate(event)
        },
        onEditCustomFieldNavigate = { title: String, index: Int, _: Option<Int> ->
            val event =
                NoteCustomFieldNavigation(CustomFieldNavigation.EditCustomField(title, index))
            onNavigate(event)
        },
        onRemoveCustomFieldNavigate = {
            val event = NoteCustomFieldNavigation(CustomFieldNavigation.RemovedCustomField)
            onNavigate(event)
        },
        onDismissBottomsheet = { onNavigate(CreateNoteNavigation.DismissBottomsheet) }
    )
    customFieldNameDialogGraph(CustomFieldPrefix.CreateNote) {
        when (it) {
            is CustomFieldNameNavigation.CloseScreen -> {
                onNavigate(CreateNoteNavigation.CloseScreen)
            }
        }
    }
    createTotpGraph(
        prefix = CustomFieldPrefix.CreateNote,
        onSuccess = { totp, _, index ->
            val values = buildMap<String, Any> {
                put(TOTP_NAV_PARAMETER_KEY, totp)
                index?.let { put(INDEX_NAV_PARAMETER_KEY, it) }
            }
            onNavigate(CreateNoteNavigation.TotpSuccess(values))
        },
        onCloseTotp = { onNavigate(CreateNoteNavigation.TotpCancel) },
        onOpenImagePicker = { _, index ->
            onNavigate(CreateNoteNavigation.OpenImagePicker(index.toOption()))
        }
    )
}

sealed interface CreateNoteNavigation {
    data class SelectVault(val shareId: ShareId) : CreateNoteNavigation
    data object NoteCreated : CreateNoteNavigation
    data object AddAttachment : CreateNoteNavigation
    data object Upgrade : CreateNoteNavigation
    data object DismissBottomsheet : CreateNoteNavigation

    @JvmInline
    value class DeleteAllAttachments(val attachmentIds: Set<AttachmentId>) : CreateNoteNavigation
    data object CloseScreen : CreateNoteNavigation

    @JvmInline
    value class OpenDraftAttachmentOptions(val uri: URI) : CreateNoteNavigation

    data object UpsellAttachments : CreateNoteNavigation

    @JvmInline
    value class NoteCustomFieldNavigation(val event: CustomFieldNavigation) : CreateNoteNavigation

    @JvmInline
    value class TotpSuccess(val results: Map<String, Any>) : CreateNoteNavigation
    data object TotpCancel : CreateNoteNavigation

    @JvmInline
    value class OpenImagePicker(val index: Option<Int>) : CreateNoteNavigation
}
