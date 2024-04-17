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

package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.item.icon.ThreeDotsMenuButton

@Composable
internal fun LoginMonitorWidget(
    modifier: Modifier = Modifier,
    model: LoginMonitorWidgetModel,
    onMenuActionClick: (LoginDetailEvent) -> Unit
) = with(model) {
    Row(
        modifier = modifier
            .roundedContainer(
                backgroundColor = PassTheme.colors.noteInteractionNormMinor2,
                borderColor = PassTheme.colors.noteInteractionNormMinor2,
            )
            .padding(all = Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        Icon(
            modifier = Modifier
                .size(size = 24.dp)
                .clip(shape = RoundedCornerShape(Radius.small))
                .background(color = PassTheme.colors.noteInteractionNormMinor1)
                .padding(all = Spacing.extraSmall),
            painter = painterResource(id = R.drawable.ic_exclamation_mark),
            contentDescription = null,
            tint = PassTheme.colors.noteInteractionNormMajor2
        )

        Column(
            modifier = Modifier.weight(weight = 1f),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            Text(
                text = stringResource(id = titleResId),
                color = PassTheme.colors.noteInteractionNormMajor2,
                style = ProtonTheme.typography.body1Medium
            )

            Text(
                text = stringResource(id = subtitleResId),
                color = PassTheme.colors.noteInteractionNormMajor2,
                style = PassTheme.typography.body3Norm()
            )
        }

        Column {
            var isDropdownMenuExpanded by remember { mutableStateOf(false) }

            ThreeDotsMenuButton(
                dotsColor = PassTheme.colors.noteInteractionNormMajor2,
                dotsSize = 18.dp,
                onClick = { isDropdownMenuExpanded = true }
            )

            DropdownMenu(
                modifier = Modifier.background(color = PassTheme.colors.backgroundStrong),
                expanded = isDropdownMenuExpanded,
                onDismissRequest = { isDropdownMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        isDropdownMenuExpanded = false
                        
                        onMenuActionClick(menuActionEvent)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium)
                    ) {
                        Text(
                            text = stringResource(id = menuActionTextResId),
                            style = PassTheme.typography.body3Weak()
                        )

                        Icon(
                            painter = painterResource(id = menuActionIconResId),
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}
