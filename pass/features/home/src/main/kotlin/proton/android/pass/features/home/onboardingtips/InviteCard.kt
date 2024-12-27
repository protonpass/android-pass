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

package proton.android.pass.features.home.onboardingtips

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.PendingInvite
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.features.home.R

@Composable
internal fun InviteCard(
    modifier: Modifier = Modifier,
    pendingInvite: PendingInvite,
    onClick: () -> Unit
) {
    val (titleResId, bodyResId) = remember(pendingInvite) {
        when (pendingInvite) {
            is PendingInvite.Item -> {
                R.string.home_item_invite_banner_title to R.string.home_item_invite_banner_subtitle
            }

            is PendingInvite.Vault -> {
                R.string.home_invite_banner_title to R.string.home_invite_banner_subtitle
            }
        }
    }

    SpotlightCard(
        modifier = modifier,
        backgroundColor = PassTheme.colors.backgroundMedium,
        title = stringResource(id = titleResId, pendingInvite.inviterEmail),
        body = stringResource(id = bodyResId),
        titleColor = PassTheme.colors.textNorm,
        image = {
            Image(
                modifier = Modifier.size(60.dp),
                alignment = Alignment.CenterEnd,
                painter = painterResource(id = R.drawable.spotlight_invite),
                contentDescription = null
            )
        },
        buttonText = null,
        onClick = onClick,
        onDismiss = null
    )
}

@[Preview Composable]
internal fun InviteCardPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            InviteCard(
                pendingInvite = PendingInvite.Vault(
                    inviteToken = InviteToken(""),
                    inviterEmail = "inviter@email.com",
                    invitedAddressId = "invitedAddressId",
                    isFromNewUser = false,
                    memberCount = 0,
                    itemCount = 0,
                    name = "Vault name",
                    icon = ShareIcon.Icon1,
                    color = ShareColor.Color1
                ),
                onClick = {}
            )
        }
    }
}
