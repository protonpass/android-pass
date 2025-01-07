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

package proton.android.pass.features.itemdetail.login.widgets

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.features.itemdetail.R
import proton.android.pass.features.itemdetail.login.LoginMonitorWidget

@Composable
internal fun LoginMonitorInsecurePassWidget(modifier: Modifier = Modifier) {
    LoginMonitorWidget(
        modifier = modifier,
        title = stringResource(id = R.string.login_item_monitor_widget_weak_pass_title),
        subtitleResId = R.string.login_item_monitor_widget_weak_pass_subtitle
    )
}

@[Preview Composable]
internal fun LoginMonitorInsecurePassWidgetPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            LoginMonitorInsecurePassWidget()
        }
    }
}
