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

package proton.android.pass.features.sharing.sharingwith

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.SharePermission
import proton.android.pass.domain.SharePermissionFlag
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.VaultId
import java.util.Date
import me.proton.core.presentation.R as CoreR

@Composable
internal fun CustomizeVault(
    modifier: Modifier = Modifier,
    vaultShare: Share.Vault,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .roundedContainerNorm()
            .padding(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        VaultIcon(
            backgroundColor = vaultShare.color.toColor(isBackground = true),
            iconColor = vaultShare.color.toColor(isBackground = false),
            icon = vaultShare.icon.toResource()
        )

        Text(
            text = vaultShare.name,
            style = ProtonTheme.typography.defaultSmallNorm,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Circle(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onClick() },
            backgroundColor = PassTheme.colors.interactionNormMinor1
        ) {
            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_pencil),
                tint = PassTheme.colors.interactionNormMajor2,
                contentDescription = null
            )
        }
    }
}

@[Preview Composable]
internal fun CustomizeVaultPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            CustomizeVault(
                vaultShare = Share.Vault(
                    userId = UserId(id = ""),
                    id = ShareId("1234"),
                    vaultId = VaultId("123"),
                    groupId = null,
                    groupEmail = null,
                    name = "Vault name",
                    createTime = Date(),
                    targetId = "target-id",
                    permission = SharePermission.fromFlags(listOf(SharePermissionFlag.Admin)),
                    expirationTime = null,
                    shareRole = ShareRole.Admin,
                    color = ShareColor.Color1,
                    icon = ShareIcon.Icon1,
                    isOwner = true,
                    memberCount = 1,
                    shared = false,
                    maxMembers = 11,
                    pendingInvites = 0,
                    newUserInvitesReady = 0,
                    canAutofill = true,
                    shareFlags = ShareFlags(0)
                ),
                onClick = {}
            )
        }
    }
}
