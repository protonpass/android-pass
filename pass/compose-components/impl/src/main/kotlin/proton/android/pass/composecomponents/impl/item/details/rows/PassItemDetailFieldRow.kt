/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.composecomponents.impl.item.details.rows

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.composecomponents.impl.item.SectionSubtitle
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.composecomponents.impl.utils.PassItemColors

@Composable
internal fun PassItemDetailFieldRow(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int?,
    title: String,
    subtitle: String,
    itemColors: PassItemColors,
    isSelectable: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentInBetween: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .applyIf(
                condition = onClick != null,
                ifTrue = { clickable(onClick = onClick!!) }
            )
            .padding(all = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        icon?.let { iconResId ->
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = itemColors.norm
            )
        }

        Column(
            modifier = Modifier.weight(weight = 1f)
        ) {
            SectionTitle(
                modifier = Modifier.padding(start = Spacing.small),
                text = title
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            if (isSelectable) {
                SelectionContainer {
                    SectionSubtitle(
                        modifier = Modifier.padding(start = Spacing.small),
                        text = subtitle.asAnnotatedString()
                    )
                }
            } else {
                SectionSubtitle(
                    modifier = Modifier.padding(start = Spacing.small),
                    text = subtitle.asAnnotatedString()
                )
            }
        }

        contentInBetween?.invoke()
    }

}
