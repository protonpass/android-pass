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

package proton.android.pass.features.security.center.shared.ui.rows

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonui.api.asAnnotatedString
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.item.SectionSubtitle
import proton.android.pass.composecomponents.impl.item.SectionTitle

@Composable
internal fun SecurityCenterRow(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    isClickable: Boolean,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    accentBackgroundColor: Color? = null
) {
    Column(
        modifier = modifier
            .roundedContainerNorm()
            .applyIf(
                condition = accentBackgroundColor != null,
                ifTrue = { background(color = accentBackgroundColor!!.copy(alpha = 0.4F)) }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .applyIf(
                    condition = isClickable && onClick != null,
                    ifTrue = { clickable(onClick = onClick!!) }
                )
                .padding(all = Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {

            leadingContent?.invoke()

            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .padding(start = Spacing.extraSmall),
                verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
            ) {
                SectionSubtitle(text = title.asAnnotatedString())

                SectionTitle(text = subtitle)
            }

            trailingContent?.invoke()
        }
    }
}

@[Preview Composable]
fun SecurityCenterRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SecurityCenterRow(
                title = "Security center row counter title",
                subtitle = "Security center row counter subtitle",
                isClickable = false
            )
        }
    }
}
