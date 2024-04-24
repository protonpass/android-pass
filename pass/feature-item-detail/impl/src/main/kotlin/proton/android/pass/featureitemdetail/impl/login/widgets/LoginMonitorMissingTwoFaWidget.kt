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

package proton.android.pass.featureitemdetail.impl.login.widgets

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.featureitemdetail.impl.login.LoginMonitorWidget

@Composable
internal fun LoginMonitorMissingTwoFaWidget(modifier: Modifier = Modifier) {
    LoginMonitorWidget(
        modifier = modifier,
        titleResId = R.string.login_item_monitor_widget_missing_two_fa_title,
        subtitleResId = R.string.login_item_monitor_widget_missing_two_fa_subtitle,
        itemCategory = ItemCategory.Login
    )
}

@[Preview Composable]
internal fun LoginMonitorMissingTwoFaWidgetPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            LoginMonitorMissingTwoFaWidget()
        }
    }
}
