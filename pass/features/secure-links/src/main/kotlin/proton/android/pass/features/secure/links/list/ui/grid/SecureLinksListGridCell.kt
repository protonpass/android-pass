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

package proton.android.pass.features.secure.links.list.ui.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.composecomponents.impl.item.icon.IdentityIcon
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.android.pass.composecomponents.impl.utils.passRemainingTimeText
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.domain.time.RemainingTime
import proton.android.pass.features.secure.links.R
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SecureLinksListGridCell(
    modifier: Modifier = Modifier,
    itemCategory: ItemCategory,
    title: String,
    website: String?,
    packageName: String?,
    canLoadExternalImages: Boolean,
    expiration: Option<RemainingTime>,
    views: Int,
    onCellClick: () -> Unit,
    onCellOptionsClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = PassTheme.colors.interactionNormMinor2,
                shape = RoundedCornerShape(size = Radius.medium)
            )
            .clickable { onCellClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(
                    horizontal = Spacing.medium,
                    vertical = Spacing.medium.plus(Spacing.small)
                )
                .align(alignment = Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            SecureLinksListGridCellIcon(
                itemCategory = itemCategory,
                itemTitle = title,
                itemWebsite = website,
                itemPackageName = packageName,
                canLoadExternalImages = canLoadExternalImages
            )

            SecureLinksListGridCellInfo(
                title = title,
                expiration = expiration,
                views = views
            )
        }

        SecureLinksGridCellMenu(
            onClick = onCellOptionsClick
        )
    }
}

@Composable
private fun SecureLinksListGridCellIcon(
    modifier: Modifier = Modifier,
    itemCategory: ItemCategory,
    itemTitle: String,
    itemWebsite: String?,
    itemPackageName: String?,
    canLoadExternalImages: Boolean
) = when (itemCategory) {
    ItemCategory.CreditCard -> CreditCardIcon(modifier = modifier)
    ItemCategory.Identity -> IdentityIcon(modifier = modifier)
    ItemCategory.Note -> NoteIcon(modifier = modifier)
    ItemCategory.Login -> LoginIcon(
        modifier = modifier,
        text = itemTitle,
        canLoadExternalImages = canLoadExternalImages,
        website = itemWebsite,
        packageName = itemPackageName
    )

    ItemCategory.Alias,
    ItemCategory.Password,
    ItemCategory.Unknown -> {
    }
}

@Composable
private fun SecureLinksListGridCellInfo(
    modifier: Modifier = Modifier,
    title: String,
    expiration: Option<RemainingTime>,
    views: Int
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
    ) {
        Text(
            text = title,
            style = ProtonTheme.typography.body2Medium,
            color = PassTheme.colors.textNorm,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = when (expiration) {
                None -> {
                    stringResource(id = R.string.secure_links_list_unknown_expiration_description)
                }

                is Some -> {
                    stringResource(
                        id = R.string.secure_links_list_expiration_description,
                        passRemainingTimeText(remainingTime = expiration.value)
                            ?: stringResource(id = R.string.secure_links_list_unknown_expiration_description)
                    )
                }
            },
            style = ProtonTheme.typography.captionRegular,
            color = PassTheme.colors.textWeak
        )

        Text(
            text = pluralStringResource(
                id = R.plurals.secure_links_list_viewed_times_description,
                count = views,
                views
            ),
            style = ProtonTheme.typography.captionRegular,
            color = PassTheme.colors.textWeak
        )
    }
}


@Composable
private fun BoxScope.SecureLinksGridCellMenu(modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(
        modifier = modifier.align(alignment = Alignment.TopEnd),
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = CoreR.drawable.ic_proton_three_dots_vertical),
            contentDescription = null,
            tint = ProtonTheme.colors.iconWeak
        )
    }
}

@[Preview Composable]
internal fun SecureLinksListGridCellPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            SecureLinksListGridCell(
                itemCategory = ItemCategory.Login,
                canLoadExternalImages = false,
                title = "Link title",
                website = null,
                packageName = null,
                expiration = None,
                views = 2,
                onCellClick = {},
                onCellOptionsClick = {}
            )
        }
    }
}
