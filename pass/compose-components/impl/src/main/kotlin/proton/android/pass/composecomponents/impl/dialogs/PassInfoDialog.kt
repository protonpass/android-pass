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

package proton.android.pass.composecomponents.impl.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.text.Text
import me.proton.core.presentation.R as CoreR

@Composable
fun PassInfoDialog(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    NoPaddingDialog(
        modifier = modifier,
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = PassTheme.colors.backgroundWeak)
                .padding(
                    start = Spacing.large,
                    top = Spacing.large,
                    end = Spacing.large,
                    bottom = Spacing.medium
                ),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.large)
        ) {
            Text.Headline(text = title)

            Text.Body1Regular(text = message)

            TextButton(
                modifier = Modifier
                    .align(alignment = Alignment.End)
                    .offset(x = Spacing.medium),
                onClick = onDismiss
            ) {
                Text.Body1Medium(
                    text = stringResource(id = CoreR.string.presentation_alert_ok),
                    color = PassTheme.colors.interactionNormMajor2
                )
            }
        }
    }
}

@[Preview Composable]
internal fun PassInfoDialogPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PassInfoDialog(
                title = "Info title",
                message = "This is an informative message to explain something",
                onDismiss = {}
            )
        }
    }
}
