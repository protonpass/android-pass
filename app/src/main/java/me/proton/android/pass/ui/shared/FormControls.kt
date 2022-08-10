package me.proton.android.pass.ui.shared

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.android.pass.R
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.caption

@Composable
fun ProtonFormInput(
    @StringRes title: Int,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes placeholder: Int? = null,
    required: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    moveToNextOnEnter: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    Column(modifier = modifier) {
        ProtonTextTitle(title)
        ProtonTextField(
            value = value,
            onChange = onChange,
            placeholder = placeholder,
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            moveToNextOnEnter = moveToNextOnEnter,
            modifier = Modifier.padding(top = 8.dp),
        )
        if (required) {
            Text(
                text = stringResource(R.string.field_required_indicator),
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 12.sp,
                style = ProtonTheme.typography.caption,
                color = ProtonTheme.colors.textWeak
            )
        }
    }
}

@Composable
fun ProtonTextTitle(
    @StringRes title: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(title),
        color = ProtonTheme.colors.textNorm,
        style = ProtonTheme.typography.caption,
        fontWeight = FontWeight.W500,
        fontSize = 12.sp,
        modifier = modifier,
    )
}

@Composable
fun ProtonTextField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes placeholder: Int? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    moveToNextOnEnter: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
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

    TextField(
        value = value,
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
        modifier = modifier.fillMaxWidth(),
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
        )
    )
}

@Composable
fun ProtonTextFieldPlaceHolder(
    @StringRes placeholder: Int? = null
) {
    if (placeholder == null) return
    Text(
        text = stringResource(placeholder),
        color = ProtonTheme.colors.textHint,
        fontSize = 16.sp,
        fontWeight = FontWeight.W400
    )
}
