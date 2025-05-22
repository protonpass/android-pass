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
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import me.proton.core.presentation.R as CoreR

@Composable
internal fun TextCustomFieldEntry(
    modifier: Modifier = Modifier,
    content: UICustomFieldContent.Text,
    index: Int,
    canEdit: Boolean,
    onChange: (String) -> Unit,
    onFocusChange: (Int, Boolean) -> Unit,
    onOptionsClick: () -> Unit,
    showLeadingIcon: Boolean,
    passItemColors: PassItemColors
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
        textFieldModifier = Modifier
            .fillMaxWidth()
            .applyIf(
                condition = !showLeadingIcon,
                ifTrue = { padding(start = Spacing.medium) }
            ),
        textStyle = ProtonTheme.typography.defaultNorm(canEdit),
        label = { ProtonTextFieldLabel(text = content.label) },
        placeholder = { ProtonTextFieldPlaceHolder(text = stringResource(R.string.custom_field_text_placeholder)) },
        editable = canEdit,
        value = content.value,
        onChange = onChange,
        singleLine = false,
        moveToNextOnEnter = true,
        leadingIcon = if (showLeadingIcon) {
            {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_proton_text_align_left),
                    contentDescription = null,
                    tint = PassTheme.colors.textWeak
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
                if (content.value.isNotEmpty()) {
                    SmallCrossIconButton { onChange("") }
                }

                CustomFieldOptionsButton(
                    backgroundColor = passItemColors.minorPrimary,
                    tint = passItemColors.majorSecondary,
                    onClick = onOptionsClick
                )
            }
        },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        onFocusChange = { onFocusChange(index, it) }
    )
}

@Preview
@Composable
internal fun TextCustomFieldEntryPreview(
    @PreviewParameter(ThemeCustomFieldPreviewProvider::class) input: Pair<Boolean, CustomFieldInput>
) {
    val (isDark, customFieldInput) = input

    PassTheme(isDark = isDark) {
        Surface {
            TextCustomFieldEntry(
                content = UICustomFieldContent.Text(label = "label", value = input.second.text),
                canEdit = customFieldInput.enabled,
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
