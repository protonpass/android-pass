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

package proton.android.pass.features.attachments.attachmentoptionsonedit.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.features.attachments.attachmentoptionsonedit.navigation.AttachmentOptionsOnEditNavigation
import proton.android.pass.features.attachments.attachmentoptionsonedit.presentation.AttachmentOptionsOnEditEvent
import proton.android.pass.features.attachments.attachmentoptionsonedit.presentation.AttachmentOptionsOnEditViewModel

@Composable
fun AttachmentOptionsOnEditBottomsheet(
    modifier: Modifier = Modifier,
    viewmodel: AttachmentOptionsOnEditViewModel = hiltViewModel(),
    onNavigate: (AttachmentOptionsOnEditNavigation) -> Unit
) {
    val state by viewmodel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        when (val event = state) {
            is AttachmentOptionsOnEditEvent.OpenRenameAttachment ->
                onNavigate(
                    AttachmentOptionsOnEditNavigation.OpenRenameAttachment(
                        shareId = event.shareId,
                        itemId = event.itemId,
                        attachmentId = event.attachmentId
                    )
                )

            is AttachmentOptionsOnEditEvent.OpenRenameDraftAttachment ->
                onNavigate(AttachmentOptionsOnEditNavigation.OpenRenameDraftAttachment(event.uri))

            AttachmentOptionsOnEditEvent.Close -> onNavigate(AttachmentOptionsOnEditNavigation.CloseBottomsheet)
            AttachmentOptionsOnEditEvent.Idle -> {}
        }
        viewmodel.onConsumeEvent(state)
    }

    AttachmentOptionsOnEditContent(
        modifier = modifier.bottomSheet(),
        onEvent = {
            when (it) {
                AttachmentOptionsOnEditUIEvent.Delete ->
                    viewmodel.deleteAttachment()

                AttachmentOptionsOnEditUIEvent.Rename ->
                    viewmodel.renameAttachment()
            }
        }
    )
}
