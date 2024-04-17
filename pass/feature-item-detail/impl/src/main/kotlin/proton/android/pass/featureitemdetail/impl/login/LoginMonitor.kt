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

package proton.android.pass.featureitemdetail.impl.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.featureitemdetail.impl.R

@Composable
internal fun LoginMonitor(
    modifier: Modifier = Modifier,
    isPasswordInsecure: Boolean,
    isPasswordReused: Boolean,
    isExcludedFromMonitor: Boolean
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        if (isPasswordInsecure) {
            LoginMonitorWidget(
                model = LoginMonitorWidgetModel.WeakPassword(
                    titleResId = R.string.login_item_monitor_widget_weak_pass_title,
                    subtitleResId = R.string.login_item_monitor_widget_weak_pass_subtitle,
                    isExcludedFromMonitor = isExcludedFromMonitor
                )
            )
        }

        if (isPasswordReused) {
            LoginMonitorWidget(
                model = LoginMonitorWidgetModel.ReusedPassword(
                    titleResId = R.string.login_item_monitor_widget_reused_pass_title,
                    subtitleResId = R.string.login_item_monitor_widget_reused_pass_subtitle,
                    isExcludedFromMonitor = isExcludedFromMonitor
                )
            )
        }
    }
}
