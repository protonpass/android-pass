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

package proton.android.pass.features.itemcreate.common.customfields

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.CircleButton

@Composable
internal fun AddMoreButton(
    modifier: Modifier = Modifier,
    text: String = stringResource(proton.android.pass.features.itemcreate.R.string.identity_add_more_fields),
    textColor: Color = PassTheme.colors.interactionNormMajor2,
    bgColor: Color = PassTheme.colors.interactionNormMinor1,
    onClick: () -> Unit
) {
    CircleButton(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = Spacing.small, horizontal = Spacing.medium),
        color = bgColor,
        onClick = onClick,
        content = {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(R.drawable.ic_proton_plus),
                contentDescription = null,
                tint = textColor
            )
            Spacer(modifier = Modifier.width(Spacing.small))
            Text(
                text = text,
                style = ProtonTheme.typography.captionMedium,
                color = textColor
            )
        }
    )
}
