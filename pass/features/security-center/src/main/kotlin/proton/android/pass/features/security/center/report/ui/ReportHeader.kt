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

package proton.android.pass.features.security.center.report.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.util.kotlin.toInt
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.features.security.center.report.ui.SecurityCenterReportHeaderModelUI.getCircleBackgroundColor
import proton.android.pass.features.security.center.report.ui.SecurityCenterReportHeaderModelUI.getCircleTextColor
import proton.android.pass.features.security.center.report.ui.SecurityCenterReportHeaderModelUI.getTitleText
import proton.android.pass.features.security.center.report.ui.SecurityCenterReportHeaderModelUI.getTitleTextColor
import proton.android.pass.features.security.center.shared.ui.counters.SecurityCenterCounterText

@Composable
fun ReportHeader(
    modifier: Modifier = Modifier,
    breachCount: Int,
    email: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        val model = SecurityCenterReportHeaderModel.fromBreachCount(breachCount)
        SecurityCenterCounterText(
            modifier = Modifier.size(54.dp),
            counterText = breachCount.toString(),
            backgroundColor = model.getCircleBackgroundColor(),
            textColor = model.getCircleTextColor(),
            textStyle = ProtonTheme.typography.headline
        )
        Text(
            text = model.getTitleText(),
            color = model.getTitleTextColor(),
            style = ProtonTheme.typography.body1Medium
        )
        Text(
            text = email,
            style = ProtonTheme.typography.body2Regular
        )
    }
}

@Preview
@Composable
fun ReportHeaderPreview(@PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>) {
    PassTheme(isDark = input.first) {
        Surface {
            ReportHeader(breachCount = input.second.toInt(), email = "random@proton.me")
        }
    }
}
