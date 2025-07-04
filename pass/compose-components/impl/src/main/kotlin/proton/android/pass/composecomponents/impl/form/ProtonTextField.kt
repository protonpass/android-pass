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

package proton.android.pass.composecomponents.impl.form

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionNorm
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProtonTextField(
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier.fillMaxWidth(),
    value: String,
    textStyle: TextStyle,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    moveToNextOnEnter: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    editable: Boolean = true,
    isError: Boolean = false,
    errorMessage: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    onChange: (String) -> Unit,
    onFocusChange: ((Boolean) -> Unit)? = null,
    onDoneClick: (() -> Unit)? = null,
    errorMessageModifier: Modifier = if (leadingIcon == null) {
        Modifier
    } else {
        Modifier.padding(start = 50.dp)
    }
) {
    val maxLines = if (singleLine) {
        1
    } else {
        Integer.MAX_VALUE
    }
    val focusManager = LocalFocusManager.current
    val goToNextField = {
        if (moveToNextOnEnter) {
            focusManager.moveFocus(FocusDirection.Down)
        } else {
            onDoneClick?.invoke()
        }
    }
    var hasBeenFocused: Boolean by rememberSaveable { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    Column(modifier = modifier, verticalArrangement = verticalArrangement) {
        BasicTextField(
            modifier = textFieldModifier
                .onFocusChanged { state ->
                    if (onFocusChange != null && (state.isFocused || hasBeenFocused)) {
                        hasBeenFocused = true
                        onFocusChange(state.isFocused)
                    }
                },
            value = value,
            enabled = editable,
            onValueChange = {
                if (singleLine && it.contains("\n")) {
                    // If is set to SingleLine and enter is pressed, go to the next field
                    goToNextField()
                } else {
                    onChange(it)
                }
            },
            textStyle = textStyle,
            interactionSource = interactionSource,
            visualTransformation = visualTransformation,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            keyboardActions = KeyboardActions(
                onNext = { goToNextField() },
                onDone = { goToNextField() },
                onSend = { goToNextField() }
            ),
            keyboardOptions = keyboardOptions,
            readOnly = !editable,
            cursorBrush = SolidColor(ProtonTheme.colors.textNorm),
            decorationBox = { innerTextField ->
                TextFieldDefaults.TextFieldDecorationBox(
                    value = value,
                    placeholder = placeholder,
                    visualTransformation = visualTransformation,
                    innerTextField = innerTextField,
                    singleLine = singleLine,
                    enabled = editable,
                    interactionSource = interactionSource,
                    contentPadding = PaddingValues(Spacing.none),
                    label = label,
                    trailingIcon = trailingIcon,
                    leadingIcon = leadingIcon,
                    isError = isError,
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        )
        AnimatedVisibility(
            visible = isError && errorMessage.isNotBlank(),
            label = "ProtonTextField-errorMessage"
        ) {
            Text(
                modifier = errorMessageModifier,
                text = errorMessage,
                style = ProtonTheme.typography.captionNorm,
                color = PassTheme.colors.signalDanger
            )
        }
    }
}

internal class ThemeAndProtonTextFieldProvider :
    ThemePairPreviewProvider<ProtonTextFieldPreviewData>(ProtonTextFieldPreviewProvider())

@Preview
@Composable
internal fun ProtonTextFieldPreview(
    @PreviewParameter(ThemeAndProtonTextFieldProvider::class)
    input: Pair<Boolean, ProtonTextFieldPreviewData>
) {
    PassTheme(isDark = input.first) {
        Surface {
            ProtonTextField(
                value = input.second.value,
                textStyle = ProtonTheme.typography.defaultNorm,
                placeholder = { ProtonTextFieldPlaceHolder(text = input.second.placeholder) },
                editable = input.second.isEditable,
                isError = input.second.isError,
                trailingIcon = input.second.icon,
                onChange = {}
            )
        }
    }
}
