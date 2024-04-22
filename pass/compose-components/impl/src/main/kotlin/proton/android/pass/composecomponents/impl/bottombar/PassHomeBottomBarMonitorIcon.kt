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

package proton.android.pass.composecomponents.impl.bottombar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonpresentation.api.bars.bottom.home.presentation.HomeBottomBarMonitorStatus
import proton.android.pass.domain.PlanType
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun PassHomeBottomBarMonitorIcon(
    modifier: Modifier = Modifier,
    planType: PlanType,
    monitorStatus: HomeBottomBarMonitorStatus
) {
    when (planType) {
        is PlanType.Free,
        is PlanType.Unknown -> {
            when (monitorStatus) {
                HomeBottomBarMonitorStatus.BreachIssues -> {
                    CompR.drawable.ic_shield_monitor_warning_badge to true
                }

                HomeBottomBarMonitorStatus.CheckIssues -> {
                    CompR.drawable.ic_shield_monitor_warning to false
                }

                HomeBottomBarMonitorStatus.NoIssues -> {
                    CompR.drawable.ic_shield_monitor_ok to false
                }
            }
        }

        is PlanType.Paid,
        is PlanType.Trial -> {
            when (monitorStatus) {
                HomeBottomBarMonitorStatus.BreachIssues -> {
                    CompR.drawable.ic_shield_monitor_warning_monitoring_badge to true
                }

                HomeBottomBarMonitorStatus.CheckIssues -> {
                    CompR.drawable.ic_shield_monitor_warning_monitoring to false
                }

                HomeBottomBarMonitorStatus.NoIssues -> {
                    CompR.drawable.ic_shield_monitor_ok_monitoring to false
                }
            }
        }
    }.let { (monitorIconResId, needsBadge) ->
        Box(modifier = modifier) {
            Image(
                painter = painterResource(id = monitorIconResId),
                contentDescription = stringResource(
                    id = CompR.string.bottom_bar_security_center_icon_content_description
                ),
                colorFilter = ColorFilter.tint(
                    color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current),
                    blendMode = BlendMode.SrcIn
                )
            )

            if (needsBadge) {
                Image(
                    painter = painterResource(id = CompR.drawable.ic_shield_monitor_badge),
                    contentDescription = null
                )
            }
        }
    }
}
