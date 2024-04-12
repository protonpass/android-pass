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

package proton.android.pass.features.security.center.shared.ui.bars

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar

@Composable
internal fun SecurityCenterTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    onUpClick: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit) = { }
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        onUpClick?.let { topBarOnUpClick ->
            BackArrowTopAppBar(
                onUpClick = topBarOnUpClick,
                actions = actions
            )
        }

        title?.let { topBarTitle ->
            Text(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                text = topBarTitle,
                style = PassTheme.typography.heroNorm()
            )
        }

        subtitle?.let { topBarSubtitle ->
            Text(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                text = topBarSubtitle,
                style = ProtonTheme.typography.body1Regular
            )
        }
    }
}

@Preview
@Composable
fun SecurityCenterTopBarPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SecurityCenterTopBar(title = "Title", subtitle = "Subtitle", onUpClick = {})
        }
    }
}
