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

package proton.android.pass.composecomponents.impl.attachments

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.buttons.Button
import proton.android.pass.composecomponents.impl.text.Text

@Composable
fun AddAttachmentButton(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Button.Circular(
        modifier = modifier.fillMaxWidth(),
        color = PassTheme.colors.loginInteractionNormMinor1,
        contentPadding = PaddingValues(Spacing.mediumSmall),
        enabled = isEnabled,
        onClick = onClick
    ) {
        val adjustedTextColor = if (isEnabled) {
            PassTheme.colors.loginInteractionNormMajor2
        } else {
            PassTheme.colors.loginInteractionNormMajor2.copy(
                alpha = 0.3f
            )
        }
        Text.Body1Regular(
            text = stringResource(R.string.attachment_add_file),
            color = adjustedTextColor
        )
    }
}
