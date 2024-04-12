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

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.features.security.center.R
import proton.android.pass.features.security.center.report.ui.SecurityCenterReportHeaderModel.DetectedBreaches
import proton.android.pass.features.security.center.report.ui.SecurityCenterReportHeaderModel.NoBreaches

enum class SecurityCenterReportHeaderModel {
    DetectedBreaches,
    NoBreaches;

    companion object {
        fun fromBreachCount(breachCount: Int): SecurityCenterReportHeaderModel = when {
            breachCount > 0 -> DetectedBreaches
            else -> NoBreaches
        }
    }
}

object SecurityCenterReportHeaderModelUI {

    @Composable
    fun SecurityCenterReportHeaderModel.getCircleBackgroundColor(): Color = when (this) {
        DetectedBreaches -> PassTheme.colors.passwordInteractionNormMinor1
        NoBreaches -> PassTheme.colors.backgroundMedium
    }

    @Composable
    fun SecurityCenterReportHeaderModel.getCircleTextColor(): Color = when (this) {
        DetectedBreaches -> PassTheme.colors.passwordInteractionNormMajor2
        NoBreaches -> PassTheme.colors.textNorm
    }

    @Composable
    fun SecurityCenterReportHeaderModel.getTitleTextColor(): Color = when (this) {
        DetectedBreaches -> PassTheme.colors.passwordInteractionNormMajor2
        NoBreaches -> PassTheme.colors.cardInteractionNormMajor2
    }

    @Composable
    fun SecurityCenterReportHeaderModel.getTitleText(): String = when (this) {
        DetectedBreaches -> stringResource(id = R.string.security_center_verify_report_title_detected)
        NoBreaches -> stringResource(id = R.string.security_center_verify_report_title_none)
    }
}
