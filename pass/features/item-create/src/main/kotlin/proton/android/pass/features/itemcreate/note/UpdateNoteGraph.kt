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

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.bottomsheets.customfield.customFieldBottomSheetGraph
import proton.android.pass.features.itemcreate.common.CustomFieldPrefix
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldNavigation
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameNavigation
import proton.android.pass.features.itemcreate.dialogs.customfield.customFieldNameDialogGraph
import proton.android.pass.features.itemcreate.note.BaseNoteNavigation.NoteCustomFieldNavigation
import proton.android.pass.features.itemcreate.totp.INDEX_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.TOTP_NAV_PARAMETER_KEY
import proton.android.pass.features.itemcreate.totp.createTotpGraph
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavItem
import proton.android.pass.navigation.api.composable

object UpdateNoteNavItem : NavItem(
    baseRoute = "note/edit",
    navArgIds = listOf(CommonNavArgId.ShareId, CommonNavArgId.ItemId)
) {
    fun createNavRoute(shareId: ShareId, itemId: ItemId) = "$baseRoute/${shareId.id}/${itemId.id}"
}

@Suppress("LongMethod")
fun NavGraphBuilder.updateNoteGraph(onNavigate: (BaseNoteNavigation) -> Unit) {
    composable(UpdateNoteNavItem) { navBackStack ->
        val navTotpUri by navBackStack.savedStateHandle
            .getStateFlow<String?>(TOTP_NAV_PARAMETER_KEY, null)
            .collectAsStateWithLifecycle()

        LaunchedEffect(navTotpUri) {
            navBackStack.savedStateHandle.remove<String?>(TOTP_NAV_PARAMETER_KEY)
        }

        val navTotpIndex by navBackStack.savedStateHandle
            .getStateFlow<Int?>(INDEX_NAV_PARAMETER_KEY, null)
            .collectAsStateWithLifecycle()

        LaunchedEffect(navTotpIndex) {
            navBackStack.savedStateHandle.remove<Int?>(INDEX_NAV_PARAMETER_KEY)
        }
        UpdateNote(
            navTotpUri = navTotpUri,
            navTotpIndex = navTotpIndex,
            onNavigate = onNavigate
        )
    }
    customFieldBottomSheetGraph(
        prefix = CustomFieldPrefix.UpdateNote,
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
        onDismissBottomsheet = { onNavigate(BaseNoteNavigation.DismissBottomsheet) }
    )
    customFieldNameDialogGraph(CustomFieldPrefix.UpdateNote) {
        when (it) {
            is CustomFieldNameNavigation.CloseScreen -> {
                onNavigate(BaseNoteNavigation.CloseScreen)
            }
        }
    }
    createTotpGraph(
        prefix = CustomFieldPrefix.UpdateNote,
        onSuccess = { totp, _, index ->
            val values = buildMap<String, Any> {
                put(TOTP_NAV_PARAMETER_KEY, totp)
                index?.let { put(INDEX_NAV_PARAMETER_KEY, it) }
            }
            onNavigate(BaseNoteNavigation.TotpSuccess(values))
        },
        onCloseTotp = { onNavigate(BaseNoteNavigation.TotpCancel) },
        onOpenImagePicker = { _, index ->
            onNavigate(BaseNoteNavigation.OpenImagePicker(index.toOption()))
        }
    )
}
