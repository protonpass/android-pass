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

package proton.android.pass.featureitemdetail.impl.alias

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.counter.CounterText
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.row.CounterRow
import proton.android.pass.featureitemdetail.impl.R
import me.proton.core.presentation.R as CoreR

@Composable
fun ContactsSection(
    modifier: Modifier = Modifier,
    counter: Int,
    onClick: () -> Unit
) {
    CounterRow(
        modifier = modifier.defaultMinSize(minHeight = 72.dp),
        title = stringResource(R.string.contacts),
        isClickable = true,
        onClick = onClick,
        accentBackgroundColor = PassTheme.colors.backgroundStrong,
        leadingContent = {
            Icon.Default(
                id = CoreR.drawable.ic_proton_note,
                tint = PassTheme.colors.aliasInteractionNorm
            )
        },
        trailingContent = if (counter > 0) {
            {
                CounterText(
                    text = counter.toString(),
                    backgroundColor = PassTheme.colors.backgroundMedium,
                    textColor = PassTheme.colors.textWeak
                )
            }
        } else null
    )
}

@Preview
@Composable
fun ContactsSectionPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ContactsSection(counter = 3, onClick = {})
        }
    }
}
