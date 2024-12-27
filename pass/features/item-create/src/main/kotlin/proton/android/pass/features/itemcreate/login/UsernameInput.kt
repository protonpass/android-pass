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

package proton.android.pass.features.itemcreate.login

import androidx.annotation.StringRes
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
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.features.itemcreate.R

@Composable
internal fun UsernameInput(
    modifier: Modifier = Modifier,
    value: String,
    canUpdateUsername: Boolean,
    isEditAllowed: Boolean,
    onChange: (String) -> Unit,
    onAliasOptionsClick: () -> Unit,
    onFocus: (Boolean) -> Unit,
    @StringRes labelResId: Int = R.string.field_username_title,
    @StringRes placeholderResId: Int = R.string.field_username_hint
) {
    ProtonTextField(
        modifier = modifier.padding(
            start = Spacing.none,
            top = Spacing.medium,
            end = Spacing.extraSmall,
            bottom = Spacing.medium
        ),
        value = value,
        onChange = onChange,
        moveToNextOnEnter = true,
        textStyle = ProtonTheme.typography.defaultNorm(isEditAllowed && canUpdateUsername),
        editable = isEditAllowed && canUpdateUsername,
        label = { ProtonTextFieldLabel(text = stringResource(id = labelResId)) },
        placeholder = { ProtonTextFieldPlaceHolder(text = stringResource(id = placeholderResId)) },
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

internal class ThemedUsernameInputPreviewProvider :
    ThemePairPreviewProvider<UsernameInputPreview>(UsernameInputPreviewProvider())

@Preview
@Composable
internal fun UsernameInputCanUpdateTruePreview(
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
