package me.proton.pass.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextFieldDefaults.indicatorLine
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.android.pass.ui.shared.ArrowBackIcon
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.form.ProtonTextFieldPlaceHolder

@ExperimentalComposeUiApi
@Composable
fun SearchTopBar(
    modifier: Modifier = Modifier,
    @StringRes placeholder: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onStopSearch: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    ProtonTopAppBar(
        modifier = modifier,
        title = {
            SearchTextField(
                modifier = Modifier.focusRequester(focusRequester),
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { ProtonTextFieldPlaceHolder(placeholder) }
            )
        },
        navigationIcon = { ArrowBackIcon { onStopSearch() } }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

/**
 * Copy of TextField to allow setting custom paddings
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current.copy(
        fontWeight = FontWeight.W400,
        fontSize = 16.sp
    ),
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions(),
    singleLine: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape =
        MaterialTheme.shapes.small.copy(bottomEnd = ZeroCornerSize, bottomStart = ZeroCornerSize),
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(
        backgroundColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent
    )
) {
    val textColor = textStyle.color.takeOrElse {
        colors.textColor(enabled).value
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    BasicTextField(
        value = value,
        modifier = modifier
            .background(colors.backgroundColor(enabled).value, shape)
            .indicatorLine(enabled, isError, interactionSource, colors)
            .defaultMinSize(
                minWidth = TextFieldDefaults.MinWidth,
                minHeight = TextFieldDefaults.MinHeight
            ),
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(colors.cursorColor(isError).value),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        decorationBox = @Composable { innerTextField ->
            // places leading icon, text field with label and placeholder, trailing icon
            TextFieldDefaults.TextFieldDecorationBox(
                value = value,
                visualTransformation = visualTransformation,
                innerTextField = innerTextField,
                placeholder = placeholder,
                label = label,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                singleLine = singleLine,
                enabled = enabled,
                isError = isError,
                interactionSource = interactionSource,
                colors = colors,
                contentPadding = PaddingValues(
                    start = 0.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
            )
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun SearchHomeTopBarPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDarkMode: Boolean
) {
    ProtonTheme(isDark = isDarkMode) {
        Surface {
            SearchTopBar(
                placeholder = R.string.action_search,
                searchQuery = "some search",
                onSearchQueryChange = {},
                onStopSearch = {}
            )
        }
    }
}

