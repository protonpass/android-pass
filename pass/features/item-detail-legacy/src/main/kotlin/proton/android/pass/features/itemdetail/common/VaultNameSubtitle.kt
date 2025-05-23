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

package proton.android.pass.features.itemdetail.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toSmallResource
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.SharePermission
import proton.android.pass.domain.SharePermissionFlag
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.VaultId
import java.util.Date

@Composable
fun VaultNameSubtitle(
    modifier: Modifier = Modifier,
    share: Share,
    hasMoreThanOneVaultShare: Boolean,
    onClick: () -> Unit
) {
    when (share) {
        is Share.Item -> Unit

        is Share.Vault -> {

            if (hasMoreThanOneVaultShare) {
                Row(
                    modifier = modifier
                        .border(
                            width = 1.dp,
                            color = share.color.toColor(isBackground = true),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = Spacing.small, vertical = Spacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                ) {
                    Icon(
                        modifier = Modifier.height(12.dp),
                        painter = painterResource(share.icon.toSmallResource()),
                        contentDescription = null,
                        tint = share.color.toColor()
                    )

                    Text(
                        text = AnnotatedString(share.name),
                        style = PassTheme.typography.body3Norm(),
                        color = share.color.toColor()
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun VaultNameSubtitlePreview(@PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>) {
    val members = if (input.second) 2 else 1
    PassTheme(isDark = input.first) {
        Surface {
            VaultNameSubtitle(
                share = Share.Vault(
                    userId = UserId(id = ""),
                    id = ShareId("123"),
                    vaultId = VaultId("123"),
                    name = "Vault Name",
                    color = ShareColor.Color1,
                    icon = ShareIcon.Icon1,
                    memberCount = members,
                    shared = false,
                    createTime = Date(),
                    targetId = "target-id",
                    permission = SharePermission.fromFlags(listOf(SharePermissionFlag.Admin)),
                    expirationTime = null,
                    shareRole = ShareRole.Admin,
                    isOwner = true,
                    maxMembers = 11,
                    pendingInvites = 0,
                    newUserInvitesReady = 0,
                    canAutofill = true
                ),
                onClick = {},
                hasMoreThanOneVaultShare = true
            )
        }
    }
}
