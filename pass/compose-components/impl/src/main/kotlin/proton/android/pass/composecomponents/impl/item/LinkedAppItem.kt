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

package proton.android.pass.composecomponents.impl.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.AndroidUtils.getApplicationName
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.composecomponents.impl.item.details.modifiers.contentDiff
import proton.android.pass.composecomponents.impl.item.icon.LinkedAppIcon
import proton.android.pass.domain.ItemDiffType

@Composable
fun LinkedAppItem(
    modifier: Modifier = Modifier,
    packageInfoUi: PackageInfoUi,
    isEditable: Boolean,
    onLinkedAppDelete: (PackageInfoUi) -> Unit,
    itemDiffType: ItemDiffType = ItemDiffType.None
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .contentDiff(itemDiffType)
            .applyIf(
                condition = itemDiffType == ItemDiffType.Field,
                ifTrue = {
                    padding(all = Spacing.small)
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.mediumSmall)
    ) {
        val context = LocalContext.current
        val appName = remember(packageInfoUi.packageName) {
            packageInfoUi.appName.ifBlank {
                getApplicationName(context, packageInfoUi.packageName).value()
                    ?: packageInfoUi.packageName
            }
        }

        LinkedAppIcon(
            packageName = packageInfoUi.packageName,
            shape = CircleShape,
            emptyContent = {
                Circle(backgroundColor = PassTheme.colors.loginInteractionNormMinor2) {
                    Icon(
                        painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_grid_3),
                        contentDescription = stringResource(R.string.missing_app_icon_content_description),
                        tint = PassTheme.colors.loginInteractionNorm
                    )
                }
            }
        )

        Text(
            modifier = Modifier.weight(1f),
            text = appName,
            style = ProtonTheme.typography.defaultNorm,
            color = if (itemDiffType == ItemDiffType.Content) {
                PassTheme.colors.signalWarning
            } else {
                Color.Unspecified
            }
        )

        if (isEditable) {
            SmallCrossIconButton { onLinkedAppDelete(packageInfoUi) }
        }
    }
}
