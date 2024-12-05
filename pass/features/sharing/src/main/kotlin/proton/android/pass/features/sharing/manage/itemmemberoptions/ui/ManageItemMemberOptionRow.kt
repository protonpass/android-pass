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

package proton.android.pass.features.sharing.manage.itemmemberoptions.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle

@Composable
internal fun manageItemMemberOptionRow(
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int?,
    @DrawableRes iconRes: Int,
    isSelected: Boolean,
    isEnabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
): BottomSheetItem = object : BottomSheetItem {

    val color = if (isEnabled) PassTheme.colors.textNorm else PassTheme.colors.textWeak

    override val title: @Composable () -> Unit = {
        BottomSheetItemTitle(
            text = stringResource(id = titleRes),
            color = color
        )
    }

    override val subtitle: @Composable () -> Unit = {
        subtitleRes?.let { subtitleResId ->
            BottomSheetItemSubtitle(
                text = stringResource(id = subtitleResId),
                color = PassTheme.colors.textWeak,
                maxLines = 1
            )
        }
    }

    override val leftIcon: @Composable () -> Unit = {
        BottomSheetItemIcon(
            iconId = iconRes,
            tint = color
        )
    }

    override val endIcon: @Composable () -> Unit = {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }

        if (isSelected) {
            BottomSheetItemIcon(
                iconId = R.drawable.ic_proton_checkmark,
                tint = PassTheme.colors.interactionNormMajor2
            )
        }
    }

    override val onClick: () -> Unit = {
        if (isEnabled) {
            onClick()
        }
    }

    override val isDivider = false
}
