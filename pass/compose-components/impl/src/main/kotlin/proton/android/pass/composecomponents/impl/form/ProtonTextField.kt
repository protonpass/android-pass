package proton.android.pass.composecomponents.impl.form

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
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
import me.proton.core.compose.theme.default
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProtonTextField(
    modifier: Modifier = Modifier,
    value: String,
    textStyle: TextStyle,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    moveToNextOnEnter: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    editable: Boolean = true,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onChange: (String) -> Unit,
    onFocusChange: ((Boolean) -> Unit)? = null,
) {
    val maxLines = if (singleLine) {
        1
    } else {
        Integer.MAX_VALUE
    }
    val focusManager = LocalFocusManager.current
    val goToNextField = {
        if (moveToNextOnEnter) focusManager.moveFocus(FocusDirection.Down)
    }
    var isFocused: Boolean by rememberSaveable { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    BasicTextField(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { state ->
                onFocusChange?.let { it(state.isFocused) }
                    ?: run { isFocused = state.isFocused }
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
                contentPadding = PaddingValues(0.dp),
                label = label,
                trailingIcon = trailingIcon,
                leadingIcon = leadingIcon,
                isError = isError,
                colors = TextFieldDefaults.textFieldColors(
                    disabledIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    )
}

class ThemeAndProtonTextFieldProvider :
    ThemePairPreviewProvider<ProtonTextFieldPreviewData>(ProtonTextFieldPreviewProvider())

@Preview
@Composable
fun ProtonTextFieldPreview(
    @PreviewParameter(ThemeAndProtonTextFieldProvider::class)
    input: Pair<Boolean, ProtonTextFieldPreviewData>
) {
    PassTheme(isDark = input.first) {
        Surface {
            ProtonTextField(
                value = input.second.value,
                textStyle = ProtonTheme.typography.default,
                placeholder = { ProtonTextFieldPlaceHolder(text = input.second.placeholder) },
                editable = input.second.isEditable,
                isError = input.second.isError,
                trailingIcon = input.second.icon,
                onChange = {}
            )
        }
    }
}
