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

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.features.itemcreate.R
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SSIDInput(
    modifier: Modifier = Modifier,
    text: String,
    isEditAllowed: Boolean,
    onChange: (String) -> Unit
) {
    ProtonTextField(
        modifier = modifier.padding(
            start = Spacing.none,
            top = Spacing.medium,
            end = Spacing.extraSmall,
            bottom = Spacing.medium
        ),
        value = text,
        editable = isEditAllowed,
        errorMessage = "",
        onChange = onChange,
        onFocusChange = {},
        textStyle = ProtonTheme.typography.defaultNorm(isEditAllowed),
        leadingIcon = {
            Icon.Default(
                id = CoreR.drawable.ic_proton_text_align_left,
                tint = PassTheme.colors.textWeak
            )
        },
        trailingIcon = if (text.isNotEmpty()) {
            { SmallCrossIconButton { onChange("") } }
        } else {
            null
        },
        label = {
            ProtonTextFieldLabel(
                text = stringResource(R.string.template_wifi_network_field_name)
            )
        },
        placeholder = {
            ProtonTextFieldPlaceHolder(text = stringResource(R.string.add_ssid_placeholder))
        }
    )
}
