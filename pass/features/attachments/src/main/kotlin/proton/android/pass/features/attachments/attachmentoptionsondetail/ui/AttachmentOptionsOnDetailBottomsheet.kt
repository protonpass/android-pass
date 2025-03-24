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

package proton.android.pass.features.attachments.attachmentoptionsondetail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.features.attachments.attachmentoptionsondetail.navigation.AttachmentOptionsOnDetailNavigation
import proton.android.pass.features.attachments.attachmentoptionsondetail.presentation.AttachmentOptionsOnDetailEvent
import proton.android.pass.features.attachments.attachmentoptionsondetail.presentation.AttachmentOptionsOnDetailViewModel

@Composable
fun AttachmentOptionsOnDetailBottomsheet(
    modifier: Modifier = Modifier,
    viewmodel: AttachmentOptionsOnDetailViewModel = hiltViewModel(),
    onNavigate: (AttachmentOptionsOnDetailNavigation) -> Unit
) {
    val state by viewmodel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (state.event) {
            AttachmentOptionsOnDetailEvent.Close -> onNavigate(AttachmentOptionsOnDetailNavigation.CloseBottomsheet)
            AttachmentOptionsOnDetailEvent.Idle -> {}
        }
        viewmodel.onConsumeEvent(state.event)
    }

    AttachmentOptionsOnDetailContent(
        modifier = modifier.bottomSheet(),
        canDownload = state.canDownload,
        onEvent = {
            when (it) {
                AttachmentOptionsOnDetailUIEvent.Download ->
                    viewmodel.download()

                AttachmentOptionsOnDetailUIEvent.Share ->
                    viewmodel.share()
            }
        }
    )
}
