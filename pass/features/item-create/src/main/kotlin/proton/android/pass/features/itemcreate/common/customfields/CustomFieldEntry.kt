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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemcreate.common.UICustomFieldContent

@Composable
internal fun CustomFieldEntry(
    modifier: Modifier = Modifier,
    entry: UICustomFieldContent,
    isError: Boolean,
    errorMessage: String,
    index: Int,
    canEdit: Boolean,
    onClick: () -> Unit,
    onValueChange: (String) -> Unit,
    onFocusChange: (Int, Boolean) -> Unit,
    onOptionsClick: () -> Unit,
    showLeadingIcon: Boolean = true,
    passItemColors: PassItemColors = passItemColors(itemCategory = ItemCategory.Unknown)
) {
    when (entry) {
        is UICustomFieldContent.Text -> TextCustomFieldEntry(
            modifier = modifier,
            content = entry,
            index = index,
            canEdit = canEdit,
            onChange = onValueChange,
            onFocusChange = onFocusChange,
            onOptionsClick = onOptionsClick,
            showLeadingIcon = showLeadingIcon,
            passItemColors = passItemColors
        )

        is UICustomFieldContent.Hidden -> HiddenCustomFieldEntry(
            modifier = modifier,
            content = entry,
            index = index,
            canEdit = canEdit,
            onChange = onValueChange,
            onFocusChange = onFocusChange,
            onOptionsClick = onOptionsClick,
            showLeadingIcon = showLeadingIcon,
            passItemColors = passItemColors
        )

        is UICustomFieldContent.Totp -> TotpCustomFieldEntry(
            modifier = modifier,
            content = entry,
            isError = isError,
            errorMessage = errorMessage,
            index = index,
            canEdit = canEdit,
            onChange = onValueChange,
            onFocusChange = onFocusChange,
            onOptionsClick = onOptionsClick,
            showLeadingIcon = showLeadingIcon,
            passItemColors = passItemColors
        )

        is UICustomFieldContent.Date -> DateCustomFieldEntry(
            modifier = modifier,
            content = entry,
            index = index,
            isLoading = canEdit,
            onChange = onValueChange,
            onClick = onClick,
            onFocusChange = onFocusChange,
            onOptionsClick = onOptionsClick,
            showLeadingIcon = showLeadingIcon,
            passItemColors = passItemColors
        )
    }
}
