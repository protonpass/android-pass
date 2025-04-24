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

package proton.android.pass.features.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import me.proton.core.compose.theme.overlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.searchoptions.api.SearchFilterType
import proton.android.pass.searchoptions.api.SearchFilterType.Alias
import proton.android.pass.searchoptions.api.SearchFilterType.All
import proton.android.pass.searchoptions.api.SearchFilterType.CreditCard
import proton.android.pass.searchoptions.api.SearchFilterType.Custom
import proton.android.pass.searchoptions.api.SearchFilterType.Identity
import proton.android.pass.searchoptions.api.SearchFilterType.Login
import proton.android.pass.searchoptions.api.SearchFilterType.LoginMFA
import proton.android.pass.searchoptions.api.SearchFilterType.Note
import proton.android.pass.searchoptions.api.SearchFilterType.SharedByMe
import proton.android.pass.searchoptions.api.SearchFilterType.SharedWithMe
import me.proton.core.presentation.R as CoreR

@Composable
internal fun ItemTypeFilterList(
    modifier: Modifier = Modifier,
    selected: SearchFilterType,
    itemTypeCount: ItemTypeCount,
    onItemTypeClick: (SearchFilterType) -> Unit
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        ItemTypeButton(
            All,
            selected == All,
            itemTypeCount.totalCount,
            onItemTypeClick
        )
        ItemTypeButton(Login, selected == Login, itemTypeCount.loginCount, onItemTypeClick)
        ItemTypeButton(Alias, selected == Alias, itemTypeCount.aliasCount, onItemTypeClick)
        ItemTypeButton(Note, selected == Note, itemTypeCount.noteCount, onItemTypeClick)
        ItemTypeButton(
            CreditCard,
            selected == CreditCard,
            itemTypeCount.creditCardCount,
            onItemTypeClick
        )
        ItemTypeButton(
            Identity,
            selected == Identity,
            itemTypeCount.identityCount,
            onItemTypeClick
        )

        ItemTypeButton(
            Custom,
            selected == Custom,
            itemTypeCount.customCount,
            onItemTypeClick
        )
    }
}

@Composable
private fun ItemTypeButton(
    searchFilterType: SearchFilterType,
    isSelected: Boolean,
    count: Int,
    onItemTypeClick: (SearchFilterType) -> Unit
) {
    val filterIconRes = remember(searchFilterType) {
        when (searchFilterType) {
            All -> CoreR.drawable.ic_proton_list_bullets
            Login -> CoreR.drawable.ic_proton_user
            Alias -> CoreR.drawable.ic_proton_alias
            Note -> CoreR.drawable.ic_proton_file_lines
            CreditCard -> CoreR.drawable.ic_proton_credit_card
            Identity -> CoreR.drawable.ic_proton_card_identity
            Custom -> CoreR.drawable.ic_proton_wrench
            LoginMFA -> CoreR.drawable.ic_proton_lock
            SharedWithMe -> CoreR.drawable.ic_proton_user_arrow_left
            SharedByMe -> CoreR.drawable.ic_proton_user_arrow_right
        }
    }

    val filterText = remember(searchFilterType) {
        when (searchFilterType) {
            All -> R.string.item_type_filter_all
            Login -> R.string.item_type_filter_login
            Alias -> R.string.item_type_filter_alias
            Note -> R.string.item_type_filter_note
            CreditCard -> R.string.item_type_filter_credit_card
            Identity -> R.string.item_type_filter_identity
            Custom -> R.string.item_type_filter_custom
            LoginMFA -> R.string.item_type_filter_login_totp
            SharedWithMe -> R.string.item_type_filter_items_shared_with_me
            SharedByMe -> R.string.item_type_filter_items_shared_by_me
        }
    }

    CircleButton(
        contentPadding = PaddingValues(Spacing.mediumSmall, Spacing.none),
        color = if (isSelected) {
            PassTheme.colors.interactionNormMajor1
        } else {
            PassTheme.colors.interactionNormMinor1
        },
        onClick = { onItemTypeClick(searchFilterType) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            Icon(
                modifier = Modifier.height(Spacing.medium),
                painter = painterResource(id = filterIconRes),
                contentDescription = stringResource(R.string.item_type_filter_list_icon_content_description),
                tint = if (isSelected) {
                    PassTheme.colors.textNorm
                } else {
                    PassTheme.colors.textWeak
                }
            )

            Text(
                text = stringResource(id = filterText),
                style = ProtonTheme.typography.defaultSmallNorm,
                color = PassTheme.colors.textNorm
            )

            Text(
                text = "$count",
                style = ProtonTheme.typography.overlineNorm,
                color = PassTheme.colors.textNorm
            )
        }
    }
}

@[Preview Composable]
internal fun ItemTypeFilterListPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ItemTypeFilterList(
                selected = Login,
                itemTypeCount = ItemTypeCount(
                    loginCount = 2,
                    aliasCount = 4,
                    noteCount = 1,
                    creditCardCount = 3,
                    identityCount = 2,
                    customCount = 1
                ),
                onItemTypeClick = {}
            )
        }
    }
}
