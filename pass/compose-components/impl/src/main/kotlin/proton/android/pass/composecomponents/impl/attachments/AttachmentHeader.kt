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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.buttons.Button
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import me.proton.core.presentation.R as CoreR

@Composable
fun AttachmentHeader(
    modifier: Modifier = Modifier,
    fileAmount: Int,
    isEnabled: Boolean,
    onTrashAll: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Icon.Default(
            id = CoreR.drawable.ic_proton_paper_clip,
            tint = PassTheme.colors.textWeak
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            Text.Body3Regular(stringResource(R.string.attachment_title))
            val text = if (fileAmount > 0) {
                pluralStringResource(
                    R.plurals.attachment_file_amount,
                    fileAmount,
                    fileAmount
                )
            } else {
                stringResource(R.string.attachment_no_files)
            }
            Text.Body1Weak(text)
        }
        if (fileAmount > 0 && onTrashAll != null) {
            Button.CircleIcon(
                backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
                enabled = isEnabled,
                iconId = CoreR.drawable.ic_proton_trash,
                iconTint = PassTheme.colors.loginInteractionNormMajor2,
                onClick = onTrashAll
            )
        }
    }
}
