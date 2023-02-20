package proton.android.pass.featurecreateitem.impl.login

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
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.featurecreateitem.impl.R

@Composable
internal fun UsernameInput(
    modifier: Modifier = Modifier,
    value: String,
    canUpdateUsername: Boolean,
    showCreateAliasButton: Boolean,
    isEditAllowed: Boolean,
    onChange: (String) -> Unit,
    onGenerateAliasClick: () -> Unit,
    onAliasOptionsClick: () -> Unit
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
            if (showCreateAliasButton) {
                IconButton(
                    enabled = isEditAllowed,
                    onClick = {
                        if (canUpdateUsername) {
                            onGenerateAliasClick()
                        } else {
                            onAliasOptionsClick()
                        }
                    }
                ) {
                    val buttonIcon = if (canUpdateUsername) {
                        me.proton.core.presentation.R.drawable.ic_proton_alias
                    } else {
                        me.proton.core.presentation.R.drawable.ic_proton_three_dots_vertical
                    }
                    Icon(
                        painter = painterResource(buttonIcon),
                        contentDescription = null,
                        tint = if (isEditAllowed) {
                            ProtonTheme.colors.iconNorm
                        } else {
                            ProtonTheme.colors.iconDisabled
                        }
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun UsernameInputCanUpdateTruePreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            UsernameInput(
                value = "some value",
                showCreateAliasButton = input.second,
                isEditAllowed = true,
                onChange = {},
                onGenerateAliasClick = {},
                onAliasOptionsClick = {},
                canUpdateUsername = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UsernameInputCanUpdateFalsePreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            UsernameInput(
                value = "some value",
                showCreateAliasButton = input.second,
                isEditAllowed = true,
                onChange = {},
                onGenerateAliasClick = {},
                onAliasOptionsClick = {},
                canUpdateUsername = false
            )
        }
    }
}
