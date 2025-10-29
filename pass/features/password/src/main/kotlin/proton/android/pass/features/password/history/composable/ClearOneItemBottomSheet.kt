/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.password.history.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.password.R

@Composable
fun ClearOneItemBottomSheet(modifier: Modifier = Modifier, onClearItem: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onClearItem()
            }
            .heightIn(min = 100.dp)
            .bottomSheet(horizontalPadding = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon.Default(
            id = me.proton.core.presentation.compose.R.drawable.ic_proton_trash_cross,
            tint = PassTheme.colors.textNorm
        )

        Spacer(modifier = Modifier.width(Spacing.medium))

        Text.Body2Regular(
            text = stringResource(R.string.password_history_remove_from_history),
            color = PassTheme.colors.textNorm
        )
    }
}

@Preview
@Composable
internal fun ClearOneItemBottomSheetPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ClearOneItemBottomSheet(
                onClearItem = {}
            )
        }
    }
}
