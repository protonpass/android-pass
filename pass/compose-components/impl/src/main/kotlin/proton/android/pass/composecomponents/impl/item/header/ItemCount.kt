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

package proton.android.pass.composecomponents.impl.item.header

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.body3Bold
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.searchoptions.api.SearchFilterType

@Composable
fun ItemCount(
    modifier: Modifier = Modifier,
    showSearchResults: Boolean,
    itemCount: Int?,
    itemType: SearchFilterType,
    isPinnedMode: Boolean
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
    ) {
        if (isPinnedMode) {
            Icon(
                painter = painterResource(R.drawable.ic_pin_filled),
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterVertically),
                tint = ProtonTheme.colors.iconNorm
            )
        }
        Text(
            text = if (showSearchResults) itemCount?.let { "$it" }
                ?: "0" else stringResource(getTitleCountRes(itemType, isPinnedMode)),
            style = PassTheme.typography.body3Bold()
        )
        Text(
            text = if (showSearchResults) {
                pluralStringResource(R.plurals.item_list_header_results, itemCount ?: 0)
            } else {
                itemCount?.let { "($it)" } ?: ""
            },
            style = ProtonTheme.typography.captionWeak
        )
    }
}

@StringRes
fun getTitleCountRes(itemType: SearchFilterType, isPinnedMode: Boolean): Int = when (itemType) {
    SearchFilterType.All -> if (!isPinnedMode) {
        R.string.item_list_header_all_search_results
    } else {
        R.string.item_list_header_pinned_search_results
    }

    SearchFilterType.Login -> R.string.item_list_header_logins_type
    SearchFilterType.LoginMFA -> R.string.item_list_header_logins_totp_type
    SearchFilterType.Alias -> R.string.item_list_header_aliases_type
    SearchFilterType.Note -> R.string.item_list_header_notes_type
    SearchFilterType.CreditCard -> R.string.item_list_header_credit_cards_type
    SearchFilterType.Identity -> R.string.item_list_header_identities_type
    SearchFilterType.Custom -> R.string.item_list_header_custom_type
    SearchFilterType.SharedWithMe -> R.string.item_list_header_shared_with_me
    SearchFilterType.SharedByMe -> R.string.item_list_header_shared_by_me
}

class ItemCountPreviewProvider : PreviewParameterProvider<ItemCountParameter> {
    override val values: Sequence<ItemCountParameter>
        get() = sequence {
            for (showSearchResults in listOf(false, true)) {
                for (itemCount in listOf(null, 0, 1)) {
                    yield(
                        ItemCountParameter(
                            showSearchResults = showSearchResults,
                            itemCount = itemCount
                        )
                    )
                }
            }
        }
}

data class ItemCountParameter(
    val showSearchResults: Boolean,
    val itemCount: Int?
)

class ThemedItemCountPreviewProvider :
    ThemePairPreviewProvider<ItemCountParameter>(ItemCountPreviewProvider())

@Preview
@Composable
fun ItemCountPreview(
    @PreviewParameter(ThemedItemCountPreviewProvider::class) input: Pair<Boolean, ItemCountParameter>
) {
    PassTheme(isDark = input.first) {
        Surface {
            ItemCount(
                showSearchResults = input.second.showSearchResults,
                itemCount = input.second.itemCount,
                itemType = SearchFilterType.All,
                isPinnedMode = false
            )
        }
    }
}
