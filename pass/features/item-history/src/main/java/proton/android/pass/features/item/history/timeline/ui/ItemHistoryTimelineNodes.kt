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

package proton.android.pass.features.item.history.timeline.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.item.ProtonHistoryItemRow
import proton.android.pass.composecomponents.impl.timelines.ProtonTimeline
import proton.android.pass.composecomponents.impl.timelines.ProtonTimelineNode
import proton.android.pass.composecomponents.impl.timelines.ProtonTimelineNodeConfig
import proton.android.pass.composecomponents.impl.timelines.ProtonTimelineNodeType
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.features.item.history.R
import proton.android.pass.features.item.history.navigation.ItemHistoryNavDestination
import me.proton.core.presentation.R as CoreR

@Composable
internal fun ItemHistoryTimelineNodes(
    modifier: Modifier = Modifier,
    itemRevisions: List<ItemRevision>,
    onNavigated: (ItemHistoryNavDestination) -> Unit,
) {
    itemRevisions.mapIndexed { index, itemRevision ->
        val timelineNodeVariant = createTimelineNodeVariant(index, itemRevisions.size)

        ProtonTimelineNode(
            id = itemRevision.revisionTime.toString(),
            type = timelineNodeVariant.type,
            config = ProtonTimelineNodeConfig(
                circleColor = PassTheme.colors.textWeak,
                lineBrush = SolidColor(
                    value = PassTheme.colors.textWeak,
                ),
            ),
        ) { modifier ->
            RoundedCornersColumn(
                modifier = modifier.padding(start = Spacing.small),
            ) {
                ProtonHistoryItemRow(
                    leadingIcon = painterResource(id = timelineNodeVariant.leadingIconId),
                    title = stringResource(id = timelineNodeVariant.titleId),
                    subtitle = "Yesterday, 11:19",
                    trailingIcon = timelineNodeVariant.trailingIconId?.let { id ->
                        painterResource(id = id)
                    },
                    onClick = { onNavigated(ItemHistoryNavDestination.Restore) }.takeIf {
                        timelineNodeVariant.isClickable
                    },
                )
            }
        }
    }.let { nodes ->
        ProtonTimeline(
            modifier = modifier,
            nodes = nodes.reversed(),
        )
    }
}

private data class TimelineNodeVariant(
    @DrawableRes val leadingIconId: Int,
    @StringRes val titleId: Int,
    val type: ProtonTimelineNodeType,
    val isClickable: Boolean,
    @DrawableRes val trailingIconId: Int?,
)

private fun createTimelineNodeVariant(
    index: Int,
    itemRevisionCount: Int,
): TimelineNodeVariant {
    if (itemRevisionCount == 1) {
        return TimelineNodeVariant(
            leadingIconId = CoreR.drawable.ic_proton_clock,
            titleId = R.string.item_history_timeline_node_root_title,
            type = ProtonTimelineNodeType.Unique,
            isClickable = false,
            trailingIconId = null,
        )
    }

    return when (index) {
        0 -> TimelineNodeVariant(
            leadingIconId = CoreR.drawable.ic_proton_bolt,
            titleId = R.string.item_history_timeline_node_leaf_title,
            type = ProtonTimelineNodeType.Leaf,
            isClickable = true,
            trailingIconId = CoreR.drawable.ic_proton_chevron_tiny_right,
        )

        itemRevisionCount - 1 -> TimelineNodeVariant(
            leadingIconId = CoreR.drawable.ic_proton_clock,
            titleId = R.string.item_history_timeline_node_root_title,
            type = ProtonTimelineNodeType.Root,
            isClickable = false,
            trailingIconId = null,
        )

        else -> TimelineNodeVariant(
            leadingIconId = CoreR.drawable.ic_proton_pencil,
            titleId = R.string.item_history_timeline_node_child_title,
            type = ProtonTimelineNodeType.Child,
            isClickable = true,
            trailingIconId = CoreR.drawable.ic_proton_chevron_tiny_right,
        )
    }
}
