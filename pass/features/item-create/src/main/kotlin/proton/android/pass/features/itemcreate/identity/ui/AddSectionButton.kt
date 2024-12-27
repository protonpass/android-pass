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

package proton.android.pass.features.itemcreate.identity.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.features.itemcreate.R
import me.proton.core.presentation.R as CoreR

@Composable
internal fun AddSectionButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    CircleButton(
        modifier = modifier,
        contentPadding = PaddingValues(Spacing.medium),
        color = PassTheme.colors.interactionNormMinor1,
        onClick = onClick,
        content = {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(CoreR.drawable.ic_proton_plus),
                contentDescription = null,
                tint = PassTheme.colors.interactionNormMajor2
            )
            Spacer(modifier = Modifier.width(Spacing.small))
            Text(
                text = stringResource(R.string.add_a_custom_section),
                style = ProtonTheme.typography.captionMedium,
                color = PassTheme.colors.interactionNormMajor2
            )
        }
    )
}
