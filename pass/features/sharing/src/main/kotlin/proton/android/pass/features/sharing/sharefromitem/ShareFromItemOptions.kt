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

package proton.android.pass.features.sharing.sharefromitem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultHighlightNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.sharing.R
import me.proton.core.presentation.R as CoreR

@Composable
internal fun ShareFromItemOptions(
    modifier: Modifier = Modifier,
    canUsePaidFeatures: Boolean,
    canShareViaSecureLink: Boolean,
    canManageAccess: Boolean,
    onEvent: (ShareFromItemEvent) -> Unit
) {
    Column(
        modifier = modifier.padding(
            horizontal = PassTheme.dimens.bottomsheetHorizontalPadding,
            vertical = PassTheme.dimens.bottomsheetVerticalPadding
        ),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.mediumSmall)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.sharing_from_item_title),
            style = ProtonTheme.typography.defaultHighlightNorm,
            textAlign = TextAlign.Center
        )

        ShareItemSecureLinkRow(
            iconResId = CoreR.drawable.ic_proton_user_plus,
            title = stringResource(id = R.string.share_with_user_shared_title),
            description = stringResource(id = R.string.share_with_user_shared_description),
            shouldShowPlusIcon = !canUsePaidFeatures,
            onClick = {
                if (canUsePaidFeatures) {
                    ShareFromItemEvent.ShareItem
                } else {
                    ShareFromItemEvent.UpsellItemSharing
                }.also(onEvent)
            }
        )

        if (canShareViaSecureLink) {
            ShareItemSecureLinkRow(
                iconResId = CoreR.drawable.ic_proton_link,
                title = stringResource(id = R.string.share_with_secure_link_shared_title),
                description = stringResource(id = R.string.share_with_secure_link_shared_description),
                shouldShowPlusIcon = !canUsePaidFeatures,
                onClick = {
                    if (canUsePaidFeatures) {
                        ShareFromItemEvent.ShareSecureLink
                    } else {
                        ShareFromItemEvent.UpsellSecureLink
                    }.also(onEvent)
                }
            )
        }

        if (canManageAccess) {
            ShareItemSecureLinkRow(
                iconResId = CoreR.drawable.ic_proton_users,
                title = stringResource(id = R.string.share_with_manage_shared_item_title),
                description = stringResource(id = R.string.share_with_manage_shared_item_description),
                shouldShowPlusIcon = false,
                backgroundColor = PassTheme.colors.backgroundWeak,
                onClick = { onEvent(ShareFromItemEvent.ManageSharedItem) }
            )
        }
    }
}
