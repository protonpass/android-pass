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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.DateFormatUtils
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import me.proton.core.presentation.R as CoreR

@Composable
internal fun DateCustomFieldEntry(
    modifier: Modifier = Modifier,
    content: UICustomFieldContent.Date,
    index: Int,
    isLoading: Boolean,
    onClick: () -> Unit,
    onChange: (String) -> Unit,
    onFocusChange: (Int, Boolean) -> Unit,
    onOptionsClick: () -> Unit,
    showLeadingIcon: Boolean,
    passItemColors: PassItemColors
) {
    val pattern = stringResource(R.string.custom_field_date_pattern)
    val date = remember(pattern, content.value) {
        content.value?.let {
            DateFormatUtils.formatDateFromMillis(pattern, it)
        }.orEmpty()
    }
    ProtonTextField(
        modifier = modifier
            .roundedContainerNorm()
            .clickable(onClick = onClick)
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
        textStyle = ProtonTheme.typography.defaultNorm(isLoading),
        label = { ProtonTextFieldLabel(text = content.label) },
        placeholder = { ProtonTextFieldPlaceHolder(text = stringResource(R.string.custom_field_date_placeholder)) },
        editable = false,
        value = date,
        onChange = {},
        singleLine = true,
        moveToNextOnEnter = true,
        leadingIcon = if (showLeadingIcon) {
            {
                Icon.Default(
                    id = CoreR.drawable.ic_proton_calendar_today,
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
                if (content.value != null) {
                    SmallCrossIconButton { onChange("") }
                }

                CustomFieldOptionsButton(
                    backgroundColor = passItemColors.minorPrimary,
                    tint = passItemColors.majorSecondary,
                    onClick = onOptionsClick
                )
            }
        },
        onFocusChange = { onFocusChange(index, it) }
    )
}

@Preview
@Composable
internal fun DateCustomFieldEntryPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            DateCustomFieldEntry(
                content = UICustomFieldContent.Date(label = "label", value = 1_710_150_000_000L),
                isLoading = input.second,
                index = 0,
                onChange = {},
                onClick = {},
                onFocusChange = { _, _ -> },
                onOptionsClick = {},
                showLeadingIcon = input.second,
                passItemColors = passItemColors(ItemCategory.Unknown)
            )
        }
    }
}
