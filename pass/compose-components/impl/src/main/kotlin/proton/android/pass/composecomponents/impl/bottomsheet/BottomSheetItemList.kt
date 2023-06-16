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

package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonui.api.PassTheme

@Composable
fun BottomSheetItemList(
    modifier: Modifier = Modifier,
    items: ImmutableList<BottomSheetItem>
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        items.forEach { item ->
            if (item.isDivider) {
                Divider(
                    modifier = Modifier.padding(horizontal = PassTheme.dimens.bottomsheetHorizontalPadding),
                    color = PassTheme.colors.inputBackgroundStrong
                )
            } else {
                BottomSheetItemRow(
                    title = item.title,
                    subtitle = item.subtitle,
                    leftIcon = item.leftIcon,
                    endIcon = item.endIcon,
                    onClick = item.onClick?.let { { it.invoke() } }
                )
            }
        }
    }
}
