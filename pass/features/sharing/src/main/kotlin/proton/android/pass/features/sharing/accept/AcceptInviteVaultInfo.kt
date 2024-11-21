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

package proton.android.pass.features.sharing.accept

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.common.api.SpecialCharacters
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.features.sharing.R

@Composable
internal fun AcceptInviteVaultInfo(
    modifier: Modifier = Modifier,
    vaultName: String,
    vaultItemCount: Int,
    vaultMemberCount: Int,
    vaultIcon: ShareIcon,
    vaultColor: ShareColor
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium)
    ) {
        Column(
            modifier = Modifier.align(alignment = Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VaultIcon(
                backgroundColor = vaultColor.toColor(isBackground = true),
                iconColor = vaultColor.toColor(isBackground = false),
                icon = vaultIcon.toResource(),
                size = 64,
                iconSize = 32
            )

            Text.Headline(
                modifier = Modifier.fillMaxWidth(),
                text = vaultName,
                textAlign = TextAlign.Center
            )

            val itemCount = pluralStringResource(
                R.plurals.sharing_item_count,
                vaultItemCount,
                vaultItemCount
            )
            val memberCount = pluralStringResource(
                R.plurals.sharing_member_count,
                vaultMemberCount,
                vaultMemberCount
            )
            val subtitle = remember(itemCount, memberCount) {
                "$itemCount ${SpecialCharacters.DOT_SEPARATOR} $memberCount"
            }

            Text.Body1Regular(
                text = subtitle,
                color = PassTheme.colors.textWeak
            )
        }
    }
}

@[Preview Composable]
internal fun AcceptInviteContentPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AcceptInviteVaultInfo(
                vaultName = "Some vault",
                vaultItemCount = 3,
                vaultMemberCount = 2,
                vaultIcon = ShareIcon.Icon1,
                vaultColor = ShareColor.Color1
            )
        }
    }
}
