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

package proton.android.pass.features.itemdetail.alias

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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.common.api.SpecialCharacters.DOT_SEPARATOR
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.AliasStats
import proton.android.pass.features.itemdetail.R
import me.proton.core.presentation.R as CoreR

@Composable
fun AliasStats(modifier: Modifier = Modifier, stats: AliasStats) {
    Row(
        modifier = modifier
            .roundedContainer(
                backgroundColor = Color.Transparent,
                borderColor = ProtonTheme.colors.separatorNorm
            )
            .fillMaxWidth()
            .padding(all = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        Icon.Default(
            id = CoreR.drawable.ic_proton_chart_line,
            tint = PassTheme.colors.aliasInteractionNorm
        )
        Column {
            SectionTitle(text = stringResource(R.string.activity_in_last_two_weeks))
            val forwards = pluralStringResource(
                R.plurals.alias_forwards_count,
                stats.forwardedEmails,
                stats.forwardedEmails
            )
            val replies = pluralStringResource(
                R.plurals.alias_replies_count,
                stats.repliedEmails,
                stats.repliedEmails
            )
            val blocks = pluralStringResource(
                R.plurals.alias_blocks_count,
                stats.blockedEmails,
                stats.blockedEmails
            )
            val output = listOf(forwards, replies, blocks)
            Text.Body1Regular(output.joinToString(" $DOT_SEPARATOR "))
        }
    }
}


@Preview
@Composable
fun AliasStatsPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AliasStats(stats = AliasStats(0, 0, 0))
        }
    }
}
