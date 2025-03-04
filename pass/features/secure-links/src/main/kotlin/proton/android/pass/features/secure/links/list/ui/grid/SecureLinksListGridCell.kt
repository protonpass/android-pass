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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.datetime.Clock
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.composecomponents.impl.item.icon.CustomIcon
import proton.android.pass.composecomponents.impl.item.icon.IdentityIcon
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.android.pass.composecomponents.impl.utils.passRemainingTimeText
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.domain.time.RemainingTime
import proton.android.pass.features.secure.links.R
import kotlin.time.Duration.Companion.hours
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SecureLinksListGridCell(
    modifier: Modifier = Modifier,
    itemCategory: ItemCategory,
    title: String,
    website: String?,
    packageName: String?,
    canLoadExternalImages: Boolean,
    remainingTime: RemainingTime,
    views: Int,
    onCellClick: () -> Unit,
    onCellOptionsClick: () -> Unit,
    isEnabled: Boolean,
    hasReachedMaxViewsLimit: Boolean
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = Radius.medium))
            .background(
                color = if (isEnabled) {
                    PassTheme.colors.interactionNormMinor2
                } else {
                    PassTheme.colors.textDisabled
                }
            )
            .applyIf(
                condition = isEnabled,
                ifTrue = { clickable { onCellClick() } }
            )
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
                remainingTime = remainingTime,
                views = views,
                hasReachedMaxViewsLimit = hasReachedMaxViewsLimit
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
    ItemCategory.SSHKey,
    ItemCategory.WifiNetwork,
    ItemCategory.Custom -> CustomIcon(modifier = modifier)
    ItemCategory.Alias,
    ItemCategory.Password,
    ItemCategory.Unknown -> {
    }
}

@Composable
private fun SecureLinksListGridCellInfo(
    modifier: Modifier = Modifier,
    title: String,
    remainingTime: RemainingTime,
    views: Int,
    hasReachedMaxViewsLimit: Boolean
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
            text = passRemainingTimeText(remainingTime = remainingTime)
                ?.let { remainingTimeText ->
                    stringResource(
                        id = R.string.secure_links_list_expiration_description,
                        remainingTimeText
                    )
                }
                ?: stringResource(id = R.string.secure_links_list_expired_description),
            textAlign = TextAlign.Center,
            style = ProtonTheme.typography.captionRegular,
            color = PassTheme.colors.textWeak
        )

        Text(
            text = pluralStringResource(
                id = R.plurals.secure_links_list_viewed_times_description,
                count = views,
                views
            ).let { viewsText -> if (hasReachedMaxViewsLimit) "$views/$viewsText" else viewsText },
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
internal fun SecureLinksListGridCellPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, isEnabled) = input

    PassTheme(isDark = isDark) {
        Surface {
            SecureLinksListGridCell(
                itemCategory = ItemCategory.Login,
                canLoadExternalImages = false,
                title = "Link title",
                website = null,
                packageName = null,
                remainingTime = RemainingTime(
                    endInstant = Clock.System.now() + 1.hours
                ),
                views = 2,
                onCellClick = {},
                onCellOptionsClick = {},
                isEnabled = isEnabled,
                hasReachedMaxViewsLimit = !isEnabled
            )
        }
    }
}
