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

package proton.android.pass.features.alias.contacts.detail.ui

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.alias.contacts.detail.presentation.DetailAliasContactUIEvent

@Composable
fun QuestionMarkRoundedIcon(modifier: Modifier = Modifier, onEvent: (DetailAliasContactUIEvent) -> Unit) {
    CircleIconButton(
        modifier = modifier,
        size = 30.dp,
        backgroundColor = PassTheme.colors.aliasInteractionNormMinor1,
        onClick = { onEvent(DetailAliasContactUIEvent.Help) }
    ) {
        Text.Body1Regular(
            text = "${SpecialCharacters.QUESTION_MARK}",
            color = PassTheme.colors.aliasInteractionNormMajor2
        )
    }
}

@Preview
@Composable
fun QuestionMarkRoundedIconPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            QuestionMarkRoundedIcon(onEvent = { })
        }
    }
}
