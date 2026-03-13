/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.composecomponents.impl.topbar

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.item.icon.ThreeDotsMenuButton
import proton.android.pass.composecomponents.impl.text.Text
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsibleSearchTopBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    title: String,
    searchQuery: String,
    placeholderText: String,
    inSearchMode: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearch: () -> Unit,
    drawerIcon: @Composable () -> Unit,
    actions: (@Composable () -> Unit)? = null
) {
    val density = LocalDensity.current
    SideEffect {
        val newLimit = with(density) {
            -(Spacing.small + 48.dp + Spacing.small - 16.dp).toPx()
        }
        if (scrollBehavior.state.heightOffsetLimit != newLimit) {
            scrollBehavior.state.heightOffsetLimit = newLimit
        }
    }

    LaunchedEffect(inSearchMode) {
        scrollBehavior.state.heightOffset = if (inSearchMode) {
            scrollBehavior.state.heightOffsetLimit
        } else {
            0f
        }
    }

    val fraction = scrollBehavior.state.collapsedFraction
    val startPadding = 16.dp
    val topPadding = 16.dp
    val endPadding = if (actions != null) Spacing.extraSmall else 16.dp
    val spacing = Spacing.small

    val trailingIcon: @Composable (() -> Unit)? = if (searchQuery.isNotEmpty()) {
        {
            IconButton(onClick = { onSearchQueryChange("") }) {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_cross),
                    contentDescription = null,
                    tint = ProtonTheme.colors.iconWeak
                )
            }
        }
    } else null

    Layout(
        modifier = modifier,
        content = {
            Box { drawerIcon() }
            Text.Headline(
                modifier = Modifier.graphicsLayer {
                    alpha = (1f - fraction * 2f).coerceAtLeast(0f)
                },
                text = title,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Box { actions?.invoke() }
            SearchTextField(
                searchQuery = searchQuery,
                placeholderText = placeholderText,
                inSearchMode = inSearchMode,
                onSearchQueryChange = onSearchQueryChange,
                onEnterSearch = onEnterSearch,
                onStopSearch = onStopSearch,
                trailingIcon = trailingIcon
            )
        }
    ) { measurables, constraints ->
        val startPx = startPadding.roundToPx()
        val topPx = topPadding.roundToPx()
        val endPx = endPadding.roundToPx()
        val spacingPx = spacing.roundToPx()
        val bottomCollapsedPx = startPx

        val iconPlaceable = measurables[0].measure(
            Constraints(maxWidth = constraints.maxWidth, maxHeight = constraints.maxHeight)
        )
        val actionsPlaceable = measurables[2].measure(
            Constraints(maxWidth = constraints.maxWidth, maxHeight = constraints.maxHeight)
        )

        val rowHeight = maxOf(iconPlaceable.height, actionsPlaceable.height)

        val titleAvailableWidth = (
            constraints.maxWidth - startPx - iconPlaceable.width - spacingPx -
                actionsPlaceable.width - endPx
            ).coerceAtLeast(0)
        val titlePlaceable = measurables[1].measure(
            Constraints(maxWidth = titleAvailableWidth, maxHeight = rowHeight)
        )

        val expandedSearchWidth = constraints.maxWidth - startPx - startPx
        val collapsedSearchWidth = (titleAvailableWidth - spacingPx).coerceAtLeast(0)
        val searchWidth = lerp(
            start = expandedSearchWidth.toFloat(),
            stop = collapsedSearchWidth.toFloat(),
            fraction = fraction
        ).roundToInt().coerceAtLeast(0)

        val searchPlaceable = measurables[3].measure(
            Constraints(minWidth = 0, maxWidth = searchWidth, maxHeight = constraints.maxHeight)
        )

        val expandedTotalH = topPx + rowHeight + spacingPx + searchPlaceable.height + spacingPx
        val collapsedTotalH = topPx + rowHeight + bottomCollapsedPx
        val totalHeight = lerp(
            start = expandedTotalH.toFloat(),
            stop = collapsedTotalH.toFloat(),
            fraction = fraction
        ).roundToInt()

        layout(constraints.maxWidth, totalHeight) {
            iconPlaceable.placeRelative(
                x = startPx,
                y = topPx + (rowHeight - iconPlaceable.height) / 2
            )

            val titleX = startPx + iconPlaceable.width + spacingPx +
                (titleAvailableWidth - titlePlaceable.width) / 2
            titlePlaceable.placeRelative(
                x = titleX,
                y = topPx + (rowHeight - titlePlaceable.height) / 2
            )

            actionsPlaceable.placeRelative(
                x = constraints.maxWidth - endPx - actionsPlaceable.width,
                y = topPx + (rowHeight - actionsPlaceable.height) / 2
            )

            val expandedSearchX = startPx
            val collapsedSearchX = startPx + iconPlaceable.width + spacingPx
            val searchX = lerp(
                start = expandedSearchX.toFloat(),
                stop = collapsedSearchX.toFloat(),
                fraction = fraction
            ).roundToInt()

            val expandedSearchY = topPx + rowHeight + spacingPx
            val collapsedSearchY = topPx + (rowHeight - searchPlaceable.height) / 2
            val searchY = lerp(
                start = expandedSearchY.toFloat(),
                stop = collapsedSearchY.toFloat(),
                fraction = fraction
            ).roundToInt()

            searchPlaceable.placeRelative(x = searchX, y = searchY)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CollapsibleSearchTopBarPreview(@PreviewParameter(ThemePreviewProvider::class) input: Boolean) {
    PassTheme(isDark = input) {
        Surface {
            CollapsibleSearchTopBar(
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
                title = "My second folder",
                searchQuery = "",
                placeholderText = "Search…",
                inSearchMode = false,
                onSearchQueryChange = {},
                onEnterSearch = {},
                onStopSearch = {},
                drawerIcon = {
                    VaultIcon(
                        backgroundColor = PassPalette.MacaroniAndCheese16,
                        iconColor = PassPalette.MacaroniAndCheese100,
                        icon = me.proton.core.presentation.R.drawable.ic_proton_house
                    )
                },
                actions = {
                    ThreeDotsMenuButton {}
                }
            )
        }
    }
}
