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

package proton.android.pass.featureprofile.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.widgets.PassSingleActionWidget
import proton.android.pass.composecomponents.impl.R as CompR
import me.proton.core.presentation.R as CoreR

@Composable
internal fun ProfileAliasesWidget(
    modifier: Modifier = Modifier,
    pendingAliasesCount: Int,
    onActionClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    PassSingleActionWidget(
        modifier = modifier.padding(horizontal = Spacing.medium),
        title = stringResource(id = CompR.string.simple_login_widget_title),
        message = pluralStringResource(
            id = CompR.plurals.simple_login_widget_pending_aliases_message,
            count = pendingAliasesCount,
            pendingAliasesCount
        ),
        actionText = stringResource(id = CompR.string.simple_login_widget_action),
        onActionClick = onActionClick,
        topRightIcon = {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = onCloseClick
                ) {
                    Icon(
                        painter = painterResource(id = CoreR.drawable.ic_close),
                        tint = PassTheme.colors.textWeak,
                        contentDescription = stringResource(
                            id = CompR.string.simple_login_widget_close_content_description
                        )
                    )
                }
            }
        }
    )
}

@[Preview Composable]
internal fun ProfileAliasesWidgetPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ProfileAliasesWidget(
                pendingAliasesCount = 16,
                onActionClick = {},
                onCloseClick = {}
            )
        }
    }
}
