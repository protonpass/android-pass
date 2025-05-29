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

package proton.android.pass.composecomponents.impl.item.details.sections.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.modifiers.contentDiff
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.ItemDiffType
import proton.android.pass.domain.ItemDiffs
import me.proton.core.presentation.R as CoreR

@Composable
internal fun PassLoginItemDetailWebsitesSection(
    modifier: Modifier = Modifier,
    websiteUrls: ImmutableList<String>,
    itemColors: PassItemColors,
    itemDiffs: ItemDiffs.Login,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) {
    val (webSectionItemDiffType, webFieldsItemDiffTypes) = remember(itemDiffs) { itemDiffs.urls }

    RoundedCornersColumn(
        modifier = modifier
            .fillMaxWidth()
            .contentDiff(itemDiffType = webSectionItemDiffType)
    ) {
        Row(
            modifier = Modifier.padding(all = Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_earth),
                contentDescription = null,
                tint = itemColors.norm
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                SectionTitle(text = stringResource(R.string.item_details_login_section_websites_title))

                websiteUrls.forEachIndexed { index, websiteUrl ->
                    PassWebsiteLinkText(
                        websiteUrl = websiteUrl,
                        onClick = { onEvent(PassItemDetailsUiEvent.OnLinkClick(websiteUrl)) },
                        onLongClick = {
                            onEvent(
                                PassItemDetailsUiEvent.OnFieldClick(
                                    field = ItemDetailsFieldType.Copyable.Website(websiteUrl)
                                )
                            )
                        },
                        itemColors = itemColors,
                        itemDiffType = webFieldsItemDiffTypes[index]
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PassWebsiteLinkText(
    modifier: Modifier = Modifier,
    websiteUrl: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    itemColors: PassItemColors,
    itemDiffType: ItemDiffType
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = Spacing.small),
        text = websiteUrl,
        color = when (itemDiffType) {
            ItemDiffType.Content,
            ItemDiffType.Field -> PassTheme.colors.signalWarning

            ItemDiffType.None -> itemColors.majorSecondary
        },
        fontSize = 16.sp,
        fontWeight = FontWeight.W400,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}
