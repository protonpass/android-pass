package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.featureitemcreate.impl.R

@Composable
internal fun UsernameInput(
    modifier: Modifier = Modifier,
    value: String,
    canUpdateUsername: Boolean,
    isEditAllowed: Boolean,
    onChange: (String) -> Unit,
    onAliasOptionsClick: () -> Unit,
    onFocus: (Boolean) -> Unit
) {
    ProtonTextField(
        modifier = modifier.padding(0.dp, 16.dp),
        value = value,
        onChange = onChange,
        textStyle = ProtonTheme.typography.default(isEditAllowed && canUpdateUsername),
        editable = isEditAllowed && canUpdateUsername,
        label = { ProtonTextFieldLabel(text = stringResource(id = R.string.field_username_title)) },
        placeholder = { ProtonTextFieldPlaceHolder(text = stringResource(id = R.string.field_username_hint)) },
        leadingIcon = {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_user),
                contentDescription = null,
                tint = ProtonTheme.colors.iconWeak
            )
        },
        trailingIcon = {
            if (canUpdateUsername) {
                if (value.isNotEmpty()) {
                    SmallCrossIconButton(enabled = isEditAllowed) { onChange("") }
                }
            } else {
                IconButton(
                    enabled = isEditAllowed,
                    onClick = { onAliasOptionsClick() }
                ) {
                    Icon(
                        painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_three_dots_vertical),
                        contentDescription = null,
                        tint = ProtonTheme.colors.iconWeak
                    )
                }
            }
        },
        onFocusChange = onFocus
    )
}

class ThemedUsernameInputPreviewProvider :
    ThemePairPreviewProvider<UsernameInputPreview>(UsernameInputPreviewProvider())

@Preview
@Composable
fun UsernameInputCanUpdateTruePreview(
    @PreviewParameter(ThemedUsernameInputPreviewProvider::class) input: Pair<Boolean, UsernameInputPreview>
) {
    PassTheme(isDark = input.first) {
        Surface {
            UsernameInput(
                value = input.second.text,
                isEditAllowed = input.second.isEditAllowed,
                canUpdateUsername = input.second.canUpdateUsername,
                onChange = {},
                onAliasOptionsClick = {},
                onFocus = {}
            )
        }
    }
}
