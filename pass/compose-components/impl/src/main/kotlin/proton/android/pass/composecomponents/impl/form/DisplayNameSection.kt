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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import me.proton.core.presentation.R as CoreR

@Composable
fun SenderNameSection(
    modifier: Modifier = Modifier,
    value: String,
    label: String = stringResource(id = R.string.field_display_name_title),
    placeholder: String = "",
    @DrawableRes icon: Int = CoreR.drawable.ic_proton_card_identity,
    enabled: Boolean = true,
    onChange: (String) -> Unit
) {
    ProtonTextField(
        modifier = modifier
            .roundedContainerNorm()
            .padding(
                start = Spacing.none,
                top = Spacing.medium,
                end = Spacing.extraSmall,
                bottom = Spacing.medium
            ),
        textStyle = ProtonTheme.typography.defaultNorm(enabled),
        label = { ProtonTextFieldLabel(text = label) },
        placeholder = { ProtonTextFieldPlaceHolder(text = placeholder) },
        editable = enabled,
        value = value,
        onChange = onChange,
        singleLine = false,
        moveToNextOnEnter = false,
        leadingIcon = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = PassTheme.colors.textWeak
            )
        },
        trailingIcon = if (value.isNotBlank() && enabled) {
            { SmallCrossIconButton { onChange("") } }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
    )
}

@Preview
@Composable
fun SenderNameSectionPreview(@PreviewParameter(ThemePreviewProvider::class) input: Boolean) {
    PassTheme(isDark = input) {
        Surface {
            SenderNameSection(
                value = "John Doe",
                onChange = {}
            )
        }
    }
}
