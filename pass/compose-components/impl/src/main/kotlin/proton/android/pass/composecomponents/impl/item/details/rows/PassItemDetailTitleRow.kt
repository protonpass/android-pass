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

package proton.android.pass.composecomponents.impl.item.details.rows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.item.details.titles.PassItemDetailSubtitle
import proton.android.pass.composecomponents.impl.item.details.titles.PassItemDetailTitle
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.pinning.BoxedPin
import proton.android.pass.composecomponents.impl.pinning.CircledPin
import proton.android.pass.composecomponents.impl.utils.ProtonItemColors
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.Vault

@Composable
internal fun PassItemDetailTitleRow(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    itemColors: ProtonItemColors,
) = with(itemUiModel) {
    when (contents) {
        is ItemContents.Alias -> with(contents as ItemContents.Alias) {
            ItemDetailTitleRow(
                modifier = modifier,
                title = title,
                isPinned = isPinned,
                itemColors = itemColors,
                vault = null,
            ) {
                AliasIcon(
                    size = 60,
                    shape = PassTheme.shapes.squircleMediumLargeShape,
                )
            }
        }

        is ItemContents.CreditCard -> with(contents as ItemContents.CreditCard) {
            ItemDetailTitleRow(
                modifier = modifier,
                title = title,
                isPinned = isPinned,
                itemColors = itemColors,
                vault = null,
            ) {
                CreditCardIcon(
                    size = 60,
                    shape = PassTheme.shapes.squircleMediumLargeShape,
                )
            }
        }

        is ItemContents.Login -> with(contents as ItemContents.Login) {
            ItemDetailTitleRow(
                modifier = modifier,
                title = title,
                isPinned = isPinned,
                itemColors = itemColors,
                vault = null,
            ) {
                LoginIcon(
                    size = 60,
                    shape = PassTheme.shapes.squircleMediumLargeShape,
                    text = title,
                    website = websiteUrl,
                    packageName = packageName,
                    canLoadExternalImages = false,
                )
            }
        }

        is ItemContents.Note -> with(contents as ItemContents.Note) {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(Spacing.large),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    ) {
                        AnimatedVisibility(
                            visible = isPinned,
                            enter = expandHorizontally(),
                        ) {
                            CircledPin(
                                ratio = 1f,
                                backgroundColor = itemColors.majorPrimary,
                            )
                        }

                        PassItemDetailTitle(
                            text = title,
                            maxLines = Int.MAX_VALUE,
                        )
                    }

                    // Uncomment this block once with pass vault param
//                    vault?.let { itemVault ->
//                        PassItemDetailSubtitle(
//                            vault = itemVault,
//                            onClick = {},
//                        )
//                    }
                }
            }
        }

        is ItemContents.Unknown -> with(contents as ItemContents.Unknown) {
            ItemDetailTitleRow(
                modifier = modifier,
                title = title,
                isPinned = false,
                itemColors = itemColors,
                vault = null,
            ) {}
        }
    }
}

@Composable
private fun ItemDetailTitleRow(
    modifier: Modifier = Modifier,
    title: String,
    isPinned: Boolean,
    itemColors: ProtonItemColors,
    vault: Vault?,
    iconContent: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        BoxedPin(
            isShown = isPinned,
            pin = {
                CircledPin(
                    backgroundColor = itemColors.majorPrimary,
                )
            },
            content = { iconContent() },
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.small),
        ) {
            PassItemDetailTitle(
                text = title,
            )

            vault?.let { itemVault ->
                PassItemDetailSubtitle(
                    vault = itemVault,
                    onClick = {},
                )
            }
        }
    }
}
