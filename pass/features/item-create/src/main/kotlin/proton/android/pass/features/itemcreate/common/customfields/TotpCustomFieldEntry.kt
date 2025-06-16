/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.itemcreate.common.customfields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.CustomFieldValidationError
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIHiddenState
import me.proton.core.presentation.R as CoreR

@Composable
internal fun TotpCustomFieldEntry(
    modifier: Modifier = Modifier,
    content: UICustomFieldContent.Totp,
    isError: Boolean,
    errorMessage: String,
    canEdit: Boolean,
    onChange: (String) -> Unit,
    onFocusChange: (Int, Boolean) -> Unit,
    onOptionsClick: () -> Unit,
    index: Int,
    showLeadingIcon: Boolean,
    passItemColors: PassItemColors
) {
    val value = when (val state = content.value) {
        is UIHiddenState.Concealed -> ""
        is UIHiddenState.Revealed -> state.clearText
        is UIHiddenState.Empty -> ""
    }

    ProtonTextField(
        modifier = modifier
            .roundedContainerNorm()
            .padding(
                start = Spacing.none,
                top = Spacing.medium,
                end = Spacing.extraSmall,
                bottom = Spacing.medium
            ),
        textFieldModifier = Modifier
            .fillMaxWidth()
            .applyIf(
                condition = !showLeadingIcon,
                ifTrue = { padding(start = Spacing.medium) }
            ),
        errorMessageModifier = Modifier.applyIf(
            condition = showLeadingIcon,
            ifTrue = { padding(start = 48.dp) },
            ifFalse = { padding(start = Spacing.medium) }
        ),
        value = value,
        onChange = onChange,
        editable = canEdit,
        isError = isError,
        errorMessage = errorMessage,
        moveToNextOnEnter = true,
        textStyle = ProtonTheme.typography.defaultNorm(canEdit)
            .copy(fontFamily = FontFamily.Monospace),
        onFocusChange = { onFocusChange(index, it) },
        label = { ProtonTextFieldLabel(text = content.label, isError = isError) },
        placeholder = {
            ProtonTextFieldPlaceHolder(text = stringResource(id = R.string.totp_create_login_field_placeholder))
        },
        leadingIcon = if (showLeadingIcon) {
            {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_proton_lock),
                    contentDescription = stringResource(R.string.mfa_icon_content_description),
                    tint = if (isError) {
                        PassTheme.colors.signalDanger
                    } else {
                        ProtonTheme.colors.iconWeak
                    }
                )
            }
        } else {
            null
        },
        trailingIcon = {
            Row(
                modifier = Modifier.padding(end = Spacing.small),
                horizontalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
            ) {
                if (value.isNotEmpty()) {
                    SmallCrossIconButton { onChange("") }
                }

                CustomFieldOptionsButton(
                    backgroundColor = passItemColors.minorPrimary,
                    tint = passItemColors.majorSecondary,
                    onClick = onOptionsClick
                )
            }
        }
    )
}

@Preview
@Composable
internal fun TotpCustomFieldEntryPreview(
    @PreviewParameter(ThemeTotpCustomFieldInput::class) input: Pair<Boolean, TotpCustomFieldInput>
) {
    val (isDark, customFieldInput) = input

    PassTheme(isDark = isDark) {
        Surface {
            TotpCustomFieldEntry(
                content = UICustomFieldContent.Totp(
                    label = "label",
                    value = UIHiddenState.Revealed("", customFieldInput.text),
                    id = "id"
                ),
                isError = customFieldInput.error != null,
                errorMessage = when (customFieldInput.error) {
                    is CustomFieldValidationError.InvalidTotp ->
                        stringResource(R.string.totp_create_login_field_invalid)

                    null -> ""
                },
                canEdit = customFieldInput.isEnabled,
                index = 0,
                onChange = {},
                onFocusChange = { _, _ -> },
                onOptionsClick = {},
                showLeadingIcon = customFieldInput.showLeadingIcon,
                passItemColors = passItemColors(ItemCategory.Unknown)
            )
        }
    }
}
