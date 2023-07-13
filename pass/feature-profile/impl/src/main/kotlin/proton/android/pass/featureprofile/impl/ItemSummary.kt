/*
 * Copyright (c) 2023 Proton AG
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

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.headlineHint
import me.proton.core.compose.theme.headlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.item.icon.MFAIcon
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon

@Composable
fun ItemSummary(
    modifier: Modifier = Modifier,
    itemSummaryUiState: ItemSummaryUiState
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        ItemTypeBox(type = SummaryItemType.Logins, count = itemSummaryUiState.loginCount)
        ItemTypeBox(
            type = SummaryItemType.Alias,
            count = itemSummaryUiState.aliasCount,
            limit = itemSummaryUiState.aliasLimit
        )
        ItemTypeBox(
            type = SummaryItemType.CreditCards,
            count = itemSummaryUiState.creditCardsCount
        )
        ItemTypeBox(type = SummaryItemType.Notes, count = itemSummaryUiState.notesCount)
        ItemTypeBox(
            type = SummaryItemType.MFA,
            count = itemSummaryUiState.mfaCount,
            limit = itemSummaryUiState.mfaLimit
        )
        Spacer(modifier = Modifier.width(8.dp))
    }
}


@Composable
fun RowScope.ItemTypeBox(
    modifier: Modifier = Modifier,
    type: SummaryItemType,
    count: Int,
    limit: Int? = null
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .border(1.dp, PassTheme.colors.inputBorderNorm, CircleShape)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (type) {
            SummaryItemType.Logins -> LoginIcon(shape = CircleShape)
            SummaryItemType.Notes -> NoteIcon(shape = CircleShape)
            SummaryItemType.CreditCards -> CreditCardIcon(shape = CircleShape)
            SummaryItemType.Alias -> AliasIcon(shape = CircleShape)
            SummaryItemType.MFA -> MFAIcon(shape = CircleShape)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = count.toString(),
            style = ProtonTheme.typography.headlineNorm
        )
        if (limit != null) {
            Text(
                text = "/$limit",
                style = ProtonTheme.typography.headlineHint
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
    }
}

enum class SummaryItemType {
    Logins, Notes, CreditCards, Alias, MFA
}

@Preview
@Composable
fun ItemSummaryPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            ItemSummary(
                itemSummaryUiState = ItemSummaryUiState(aliasLimit = 1),
            )
        }
    }
}
