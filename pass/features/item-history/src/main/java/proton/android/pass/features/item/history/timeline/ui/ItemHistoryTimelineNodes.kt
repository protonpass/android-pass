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
import kotlinx.datetime.Instant
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.item.PassHistoryItemRow
import proton.android.pass.composecomponents.impl.timelines.PassTimeline
import proton.android.pass.composecomponents.impl.timelines.PassTimelineNode
import proton.android.pass.composecomponents.impl.timelines.PassTimelineNodeConfig
import proton.android.pass.composecomponents.impl.timelines.PassTimelineNodeType
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.composecomponents.impl.utils.protonFormattedDateText
import proton.android.pass.data.api.repositories.ItemRevision
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.item.history.R
import proton.android.pass.features.item.history.navigation.ItemHistoryNavDestination
import me.proton.core.presentation.R as CoreR

@Composable
internal fun ItemHistoryTimelineNodes(
    modifier: Modifier = Modifier,
    shareId: ShareId,
    itemId: ItemId,
    itemRevisions: List<ItemRevision>,
    colors: PassItemColors,
    onNavigated: (ItemHistoryNavDestination) -> Unit,
) {
    itemRevisions.mapIndexed { index, itemRevision ->
        val timelineNodeVariant = createTimelineNodeVariant(index, itemRevisions.size)

        PassTimelineNode(
            id = itemRevision.revisionTime.toString(),
            type = timelineNodeVariant.type,
            config = PassTimelineNodeConfig(
                circleColor = colors.norm,
                lineBrush = SolidColor(
                    value = colors.norm,
                ),
            ),
        ) { modifier ->
            RoundedCornersColumn(
                modifier = modifier.padding(start = Spacing.small),
            ) {
                PassHistoryItemRow(
                    leadingIcon = painterResource(id = timelineNodeVariant.leadingIconId),
                    title = stringResource(id = timelineNodeVariant.titleId),
                    subtitle = protonFormattedDateText(
                        endInstant = Instant.fromEpochSeconds(itemRevision.revisionTime),
                    ),
                    trailingIcon = timelineNodeVariant.trailingIconId?.let { id ->
                        painterResource(id = id)
                    },
                    onClick = {
                        onNavigated(
                            ItemHistoryNavDestination.Restore(
                                shareId = shareId,
                                itemId = itemId,
                                itemRevision = itemRevision,
                            )
                        )
                    }.takeIf { timelineNodeVariant.isClickable },
                )
            }
        }
    }.let { nodes ->
        PassTimeline(
            modifier = modifier,
            nodes = nodes,
        )
    }
}

private data class TimelineNodeVariant(
    @DrawableRes val leadingIconId: Int,
    @StringRes val titleId: Int,
    val type: PassTimelineNodeType,
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
            type = PassTimelineNodeType.Unique,
            isClickable = false,
            trailingIconId = null,
        )
    }

    return when (index) {
        0 -> TimelineNodeVariant(
            leadingIconId = CoreR.drawable.ic_proton_clock,
            titleId = R.string.item_history_timeline_node_root_title,
            type = PassTimelineNodeType.Root,
            isClickable = false,
            trailingIconId = null,
        )

        itemRevisionCount - 1 -> TimelineNodeVariant(
            leadingIconId = CoreR.drawable.ic_proton_bolt,
            titleId = R.string.item_history_timeline_node_leaf_title,
            type = PassTimelineNodeType.Leaf,
            isClickable = true,
            trailingIconId = CoreR.drawable.ic_proton_chevron_tiny_right,
        )

        else -> TimelineNodeVariant(
            leadingIconId = CoreR.drawable.ic_proton_pencil,
            titleId = R.string.item_history_timeline_node_child_title,
            type = PassTimelineNodeType.Child,
            isClickable = true,
            trailingIconId = CoreR.drawable.ic_proton_chevron_tiny_right,
        )
    }
}
