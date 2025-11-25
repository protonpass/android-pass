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
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.domain.PlanType
import proton.android.pass.preferences.monitor.MonitorStatusPreference
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun PassHomeBottomBarMonitorIcon(
    modifier: Modifier = Modifier,
    planType: PlanType,
    monitorStatus: MonitorStatusPreference
) {
    when (planType) {
        is PlanType.Free,
        is PlanType.Unknown -> {
            when (monitorStatus) {
                MonitorStatusPreference.BreachIssues -> {
                    CompR.drawable.ic_shield_monitor_warning_badge to true
                }

                MonitorStatusPreference.VulnerabilityIssues -> {
                    CompR.drawable.ic_shield_monitor_warning to false
                }

                MonitorStatusPreference.NoIssues -> {
                    CompR.drawable.ic_shield_monitor_ok to false
                }
            }
        }

        is PlanType.Paid -> {
            when (monitorStatus) {
                MonitorStatusPreference.BreachIssues -> {
                    CompR.drawable.ic_shield_monitor_warning_monitoring_badge to true
                }

                MonitorStatusPreference.VulnerabilityIssues -> {
                    CompR.drawable.ic_shield_monitor_warning_monitoring to false
                }

                MonitorStatusPreference.NoIssues -> {
                    CompR.drawable.ic_shield_monitor_ok_monitoring to false
                }
            }
        }
    }.let { (monitorIconResId, needsBadge) ->
        Box(modifier = modifier) {
            Icon(
                painter = painterResource(id = monitorIconResId),
                contentDescription = stringResource(
                    id = CompR.string.bottom_bar_security_center_icon_content_description
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

@[Preview Composable]
internal fun PassHomeBottomBarMonitorIconPreview(
    @PreviewParameter(ThemePassMonitorIconPreview::class) input: Pair<Boolean, Pair<PlanType, MonitorStatusPreference>>
) {
    val (isDark, state) = input
    val (planType, monitorStatus) = state

    PassTheme(isDark = isDark) {
        Surface {
            PassHomeBottomBarMonitorIcon(
                planType = planType,
                monitorStatus = monitorStatus
            )
        }
    }
}

internal class ThemePassMonitorIconPreview :
    ThemePairPreviewProvider<Pair<PlanType, MonitorStatusPreference>>(
        PassHomeBottomBarMonitorIconPreviewProvider()
    )

private class PassHomeBottomBarMonitorIconPreviewProvider :
    PreviewParameterProvider<Pair<PlanType, MonitorStatusPreference>> {

    override val values: Sequence<Pair<PlanType, MonitorStatusPreference>> = sequence {
        MonitorStatusPreference.entries.forEach { monitorStatusPreference ->
            yield(Pair(PlanType.Unknown(), monitorStatusPreference))
            yield(Pair(PlanType.Paid.Plus("", ""), monitorStatusPreference))
        }
    }

}

