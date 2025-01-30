/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.welcome

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.Spacing

@Composable
fun LineIndicator(modifier: Modifier = Modifier, pagerState: PagerState) {
    Row(
        modifier = modifier
            .padding(Spacing.medium)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        repeat(pagerState.pageCount) { iteration ->
            val isSelected = pagerState.currentPage == iteration
            val animatedColor by animateColorAsState(
                targetValue = if (isSelected) PassPalette.White100 else PassPalette.White10,
                animationSpec = tween(durationMillis = 300)
            )
            Box(
                modifier = Modifier
                    .padding(Spacing.extraSmall)
                    .clip(RoundedCornerShape(2.dp))
                    .background(animatedColor)
                    .weight(1f)
                    .height(6.dp)
            )
        }
    }
}
