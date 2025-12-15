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

package proton.android.pass.features.upsell.v2.presentation.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.LocalDark
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.features.upsell.v1.R
import proton.android.pass.features.upsell.v2.models.SelectorUiState
import proton.android.pass.features.upsell.v2.presentation.mocks.mockSelectors

@Composable
fun ProtonSelector(
    modifier: Modifier = Modifier,
    items: List<SelectorUiState>,
    selectedIndex: Int = 0,
    onItemSelected: (Int) -> Unit
) {
    val density = LocalDensity.current
    var toggleWidth by remember { mutableStateOf(0.dp) }

    val offsetToggle by animateDpAsState(
        targetValue = toggleWidth * selectedIndex,
        animationSpec = tween(durationMillis = 300),
        label = "selector_offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(intrinsicSize = IntrinsicSize.Min)
            .onGloballyPositioned {
                toggleWidth = with(density) { it.size.width.div(items.size).toDp() }
            }
            .background(
                color = when (LocalDark.current) {
                    true -> Color.Black.copy(alpha = 0.3f)
                    false -> Color.White.copy(alpha = 0.3f)
                },
                shape = RoundedCornerShape(percent = 50)
            )
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = offsetToggle)
                .clip(RoundedCornerShape(percent = 50))
                .fillMaxHeight()
                .fillMaxWidth(fraction = 1f.div(items.size))
                .background(Color.White)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEachIndexed { index, item ->
                OneSelector(
                    title = item.title,
                    subtitle = stringResource(
                        id = R.string.price_per_month_type1,
                        item.pricePerMonth
                    ),
                    isSelected = selectedIndex == index,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onItemSelected(index)
                    }
                )
            }
        }
    }
}

@Composable
private fun OneSelector(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    titleTextColorSelected: Color = Color(color = 0xFF201B32),
    subTitleTextColorSelected: Color = PassTheme.colors.textInvert,
    titleTextColorUnSelected: Color = ProtonTheme.colors.textNorm,
    subtitleTextColorUnSelected: Color = ProtonTheme.colors.textNorm.copy(alpha = 0.70f),
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val animatedTitleTextColor by animateColorAsState(
        targetValue = if (isSelected) titleTextColorSelected else titleTextColorUnSelected,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "text_color"
    )

    val animatedSubTitleTextColor by animateColorAsState(
        targetValue = if (isSelected) subTitleTextColorSelected else subtitleTextColorUnSelected,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "text_color"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // no ripple, the user clicks in the empty area
            ) {
                onClick()
            }
            .padding(vertical = 8.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = ProtonTheme.typography.body1Medium.copy(
                color = animatedTitleTextColor
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = animatedSubTitleTextColor,
            style = ProtonTheme.typography.body2Regular.copy(
                color = animatedSubTitleTextColor
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

}

@Composable
@Preview
fun PreviewPlanSelector(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    val (selectedIndex, onSelectedIndexChange) = remember { mutableIntStateOf(0) }
    PassTheme(isDark = isDark) {
        Surface {
            ProtonSelector(
                items = mockSelectors,
                selectedIndex = selectedIndex,
                onItemSelected = onSelectedIndexChange
            )
        }
    }
}
