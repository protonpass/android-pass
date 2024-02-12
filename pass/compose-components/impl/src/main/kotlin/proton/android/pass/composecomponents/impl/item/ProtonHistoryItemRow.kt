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

package proton.android.pass.composecomponents.impl.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonui.api.asAnnotatedString

@Composable
fun ProtonHistoryItemRow(
    leadingIcon: Painter,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailingIcon: Painter? = null,
    onClick: (() -> Unit)? = null,
    paddingValues: PaddingValues = PaddingValues(
        start = Spacing.medium,
        top = Spacing.medium,
        end = Spacing.medium,
        bottom = Spacing.medium,
    )
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .applyIf(
                condition = onClick != null,
                ifTrue = { clickable { onClick?.invoke() } }
            )
            .padding(paddingValues),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        Icon(
            painter = leadingIcon,
            contentDescription = null,
            tint = ProtonTheme.colors.textWeak,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            SectionSubtitle(text = title.asAnnotatedString())

            SectionTitle(text = subtitle)
        }

        trailingIcon?.let { painter ->
            Icon(
                painter = painter,
                contentDescription = null,
                tint = ProtonTheme.colors.textWeak,
            )
        }
    }

}
