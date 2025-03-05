/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.custom.createupdate.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.login.PASSWORD_CONCEALED_LENGTH
import me.proton.core.presentation.R as CoreR

@Composable
fun PrivateKeyInput(
    modifier: Modifier = Modifier,
    content: UIHiddenState,
    isEditAllowed: Boolean,
    onChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit
) {
    val (text, visualTransformation) = when (content) {
        is UIHiddenState.Concealed -> "x".repeat(PASSWORD_CONCEALED_LENGTH) to PasswordVisualTransformation()
        is UIHiddenState.Revealed -> content.clearText to VisualTransformation.None
        is UIHiddenState.Empty -> "" to VisualTransformation.None
    }

    Box(modifier = modifier.roundedContainerNorm()) {
        ProtonTextField(
            modifier = Modifier.padding(
                start = Spacing.none,
                top = Spacing.medium,
                end = Spacing.extraSmall,
                bottom = Spacing.medium
            ),
            value = text,
            editable = isEditAllowed,
            moveToNextOnEnter = true,
            singleLine = false,
            textStyle = ProtonTheme.typography.defaultNorm(isEditAllowed),
            onChange = onChange,
            label = { ProtonTextFieldLabel(text = stringResource(R.string.template_ssh_key_field_private_key)) },
            placeholder = {
                ProtonTextFieldPlaceHolder(text = stringResource(R.string.add_private_key_placeholder))
            },
            leadingIcon = {
                Icon.Default(
                    id = CoreR.drawable.ic_proton_key,
                    tint = ProtonTheme.colors.iconWeak
                )
            },
            trailingIcon = {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                    if (text.isNotEmpty()) {
                        SmallCrossIconButton { onChange("") }
                    }
                }
            },
            visualTransformation = visualTransformation,
            onFocusChange = { onFocusChange(it) }
        )
    }
}
