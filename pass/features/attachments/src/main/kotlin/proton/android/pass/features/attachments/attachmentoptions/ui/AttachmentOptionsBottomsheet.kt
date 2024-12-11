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

package proton.android.pass.features.attachments.attachmentoptions.ui

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.features.attachments.attachmentoptions.navigation.AttachmentOptionsNavigation
import proton.android.pass.features.attachments.attachmentoptions.presentation.AttachmentOptionsEvent
import proton.android.pass.features.attachments.attachmentoptions.presentation.AttachmentOptionsViewModel

@Composable
fun AttachmentOptionsBottomsheet(
    modifier: Modifier = Modifier,
    viewmodel: AttachmentOptionsViewModel = hiltViewModel(),
    onNavigate: (AttachmentOptionsNavigation) -> Unit
) {
    val state by viewmodel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        when (state) {
            AttachmentOptionsEvent.Close -> onNavigate(AttachmentOptionsNavigation.CloseBottomsheet)
            AttachmentOptionsEvent.Idle -> {}
        }
        viewmodel.onConsumeEvent(state)
    }

    AttachmentOptionsContent(
        modifier = modifier.bottomSheet(),
        onEvent = {
            when (it) {
                AttachmentOptionsUIEvent.Delete ->
                    viewmodel.deleteAttachment()

                AttachmentOptionsUIEvent.Open ->
                    viewmodel.openAttachment()

                AttachmentOptionsUIEvent.Rename ->
                    viewmodel.renameAttachment()
            }
        }
    )
}


@Preview
@Composable
fun AttachmentOptionsBottomsheetPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AttachmentOptionsBottomsheet(
                onNavigate = {}
            )
        }
    }
}
