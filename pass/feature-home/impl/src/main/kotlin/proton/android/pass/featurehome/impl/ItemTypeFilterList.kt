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

package proton.android.pass.featurehome.impl

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
import proton.android.pass.searchoptions.api.SearchFilterType.Identity
import proton.android.pass.searchoptions.api.SearchFilterType.Login
import proton.android.pass.searchoptions.api.SearchFilterType.LoginMFA
import proton.android.pass.searchoptions.api.SearchFilterType.Note
import me.proton.core.presentation.R as CoreR

@Composable
fun ItemTypeFilterList(
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

    }
}

@Composable
private fun ItemTypeButton(
    searchFilterType: SearchFilterType,
    isSelected: Boolean,
    count: Int,
    onItemTypeClick: (SearchFilterType) -> Unit
) {
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
                painter = when (searchFilterType) {
                    All -> painterResource(CoreR.drawable.ic_proton_list_bullets)
                    Login -> painterResource(CoreR.drawable.ic_proton_user)
                    Alias -> painterResource(CoreR.drawable.ic_proton_alias)
                    Note -> painterResource(CoreR.drawable.ic_proton_file_lines)
                    CreditCard -> painterResource(CoreR.drawable.ic_proton_credit_card)
                    Identity -> painterResource(CoreR.drawable.ic_proton_card_identity)
                    LoginMFA -> painterResource(CoreR.drawable.ic_proton_lock)
                },
                contentDescription = stringResource(R.string.item_type_filter_list_icon_content_description),
                tint = if (isSelected) {
                    PassTheme.colors.textNorm
                } else {
                    PassTheme.colors.textWeak
                }
            )
            Text(
                text = when (searchFilterType) {
                    All -> stringResource(R.string.item_type_filter_all)
                    Login -> stringResource(R.string.item_type_filter_login)
                    Alias -> stringResource(R.string.item_type_filter_alias)
                    Note -> stringResource(R.string.item_type_filter_note)
                    CreditCard -> stringResource(R.string.item_type_filter_credit_card)
                    Identity -> stringResource(R.string.item_type_filter_identity)
                    LoginMFA -> stringResource(R.string.item_type_filter_login_totp)
                },
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

@Preview
@Composable
fun ItemTypeFilterListPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            ItemTypeFilterList(
                selected = Login,
                itemTypeCount = ItemTypeCount(
                    loginCount = 2,
                    aliasCount = 4,
                    noteCount = 1,
                    creditCardCount = 3,
                    identityCount = 2
                ),
                onItemTypeClick = {}
            )
        }
    }
}
