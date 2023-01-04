package me.proton.android.pass.composecomponents.impl.form

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePairPreviewProvider

@Composable
fun ProtonTextField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onFocusChange: ((Boolean) -> Unit)? = null,
    placeholder: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    moveToNextOnEnter: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    editable: Boolean = true,
    isError: Boolean = false
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

    OutlinedTextField(
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
        placeholder = { ProtonTextFieldPlaceHolder(placeholder) },
        trailingIcon = trailingIcon,
        modifier = modifier
            .onFocusChanged { state ->
                onFocusChange?.let { it(state.isFocused) }
                    ?: run { isFocused = state.isFocused }
            }
            .border(
                width = if (isFocused) 1.dp else 0.dp,
                shape = RoundedCornerShape(8.dp),
                color = if (isFocused) ProtonTheme.colors.brandNorm else Color.Transparent
            ),
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(8.dp),
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardActions = KeyboardActions(
            onNext = { goToNextField() },
            onDone = { goToNextField() },
            onSend = { goToNextField() }
        ),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = ProtonTheme.colors.backgroundSecondary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        readOnly = !editable,
        isError = isError
    )
}

@Composable
fun ProtonTextFieldPlaceHolder(
    placeholder: String? = null
) {
    if (placeholder == null) return
    Text(
        text = placeholder,
        color = ProtonTheme.colors.textHint,
        fontSize = 16.sp,
        fontWeight = FontWeight.W400
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
    ProtonTheme(isDark = input.first) {
        Surface {
            ProtonTextField(
                value = input.second.value,
                placeholder = input.second.placeholder,
                editable = input.second.isEditable,
                isError = input.second.isError,
                trailingIcon = input.second.icon,
                onChange = {}
            )
        }
    }
}
