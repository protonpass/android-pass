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

package proton.android.pass.composecomponents.impl.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.R
import me.proton.core.presentation.R as CoreR

@Composable
fun PromoIcon(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)
) {
    ProtonTheme(isDark = false) {
        Row(
            modifier = modifier
                .height(35.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .background(PassPalette.PromoYellow)
                .padding(horizontal = Spacing.extraSmall, vertical = Spacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon.Default(
                modifier =  Modifier.fillMaxHeight(),
                id = CoreR.drawable.ic_proton_brand_proton_pass
            )
            Icon.Default(modifier = Modifier.fillMaxHeight(), id = R.drawable.ic_percentage)
        }
    }
}

@Preview
@Composable
fun PromoIconPreview() {
    PassTheme(isDark = false) {
        Surface {
            PromoIcon(
                onClick = {}
            )
        }
    }
}
