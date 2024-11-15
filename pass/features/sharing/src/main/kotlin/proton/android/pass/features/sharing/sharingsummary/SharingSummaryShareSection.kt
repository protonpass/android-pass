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

package proton.android.pass.features.sharing.sharingsummary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.text.Text

@Composable
internal fun SharingSummaryShareSection(
    modifier: Modifier = Modifier,
    sectionTitle: String,
    shareTitle: String,
    shareSubTitle: String?,
    shareIcon: @Composable () -> Unit
) {
    Column(
        modifier = modifier.padding(bottom = Spacing.large),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        Text.Body1Regular(
            text = sectionTitle,
            color = PassTheme.colors.textWeak
        )

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            shareIcon()

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
            ) {
                Text.Body1Regular(
                    text = shareTitle
                )

                shareSubTitle?.let { subTitle ->
                    Text.Body2Regular(
                        text = subTitle,
                        color = PassTheme.colors.textWeak,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
