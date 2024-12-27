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

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.form.ProtonTextField

@Composable
fun NoteTitle(
    modifier: Modifier = Modifier,
    value: String,
    enabled: Boolean,
    requestFocus: Boolean,
    onTitleRequiredError: Boolean,
    onValueChanged: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val titleColor = if (onTitleRequiredError) {
        PassTheme.colors.passwordInteractionNorm
    } else {
        PassTheme.colors.textWeak
    }

    ProtonTextField(
        modifier = modifier
            .focusRequester(focusRequester),
        textStyle = PassTheme.typography.heroNorm(enabled),
        placeholder = {
            Text(
                text = stringResource(id = R.string.field_title_title),
                style = PassTheme.typography.heroNorm(),
                color = titleColor
            )
        },
        editable = enabled,
        value = value,
        onChange = onValueChanged,
        singleLine = false,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    )

    RequestFocusLaunchedEffect(focusRequester, requestFocus)
}

class ThemeNoteTitlePreviewProvider :
    ThemePairPreviewProvider<NoteTitleInput>(NoteTitlePreviewProvider())

@Preview
@Composable
fun NoteTitlePreview(@PreviewParameter(ThemeNoteTitlePreviewProvider::class) input: Pair<Boolean, NoteTitleInput>) {
    PassTheme(isDark = input.first) {
        Surface {
            NoteTitle(
                value = input.second.text,
                enabled = input.second.enabled,
                onTitleRequiredError = input.second.isError,
                requestFocus = false,
                onValueChanged = {}
            )
        }
    }
}

