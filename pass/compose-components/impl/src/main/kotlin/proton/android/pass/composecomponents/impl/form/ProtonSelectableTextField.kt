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

package proton.android.pass.composecomponents.impl.form

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.LayoutDirection
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionNorm
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme

private const val SELECTABLE_TEXT_FIELD_ERROR_LABEL = "ProtonSelectableTextField-errorMessage"

enum class CursorSelection {
    All,
    End,
    Start,
}

@Composable
fun ProtonSelectableTextField(
    text: String,
    onTextChanged: (String) -> Unit,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    cursorSelection: CursorSelection = CursorSelection.End,
    errorText: String? = null,
    isEnabled: Boolean = true,
    isSingleLine: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val isLeftToRight = LocalLayoutDirection.current == LayoutDirection.Ltr
    val selection = when (cursorSelection) {
        CursorSelection.All -> if (isLeftToRight) TextRange.Zero else TextRange(text.length)
        CursorSelection.End -> if (isLeftToRight) TextRange(text.length) else TextRange.Zero
        CursorSelection.Start -> TextRange(0, text.length)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        BasicTextField(
            modifier = Modifier.fillMaxWidth(),
            value = TextFieldValue(text = text, selection = selection),
            onValueChange = { newTextFieldValue -> onTextChanged(newTextFieldValue.text) },
            visualTransformation = visualTransformation,
            textStyle = textStyle,
            enabled = isEnabled,
            singleLine = isSingleLine,
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions,
            cursorBrush = SolidColor(ProtonTheme.colors.textNorm),
        )

        AnimatedVisibility(
            visible = !errorText.isNullOrBlank(),
            label = SELECTABLE_TEXT_FIELD_ERROR_LABEL,
        ) {
            Text(
                text = errorText.orEmpty(),
                style = ProtonTheme.typography.captionNorm,
                color = PassTheme.colors.signalDanger,
            )
        }
    }

}

@Preview
@Composable
fun ProtonSelectableTextFieldPreview(
    @PreviewParameter(ThemedProtonTextFieldPreviewProvider::class)
    input: Pair<Boolean, ProtonSelectableTextFieldPreviewParams>,
) {
    val (isDark, params) = input

    PassTheme(isDark = isDark) {
        Surface {
            ProtonSelectableTextField(
                text = params.text,
                onTextChanged = {},
                textStyle = ProtonTheme.typography.defaultNorm,
                isEnabled = params.isEnabled,
                errorText = params.errorText,
            )
        }
    }
}
