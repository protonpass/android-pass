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

package proton.android.pass.features.sharing.sharefromitem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultHighlightNorm
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.features.sharing.R
import me.proton.core.presentation.R as CoreR

@Composable
internal fun ShareFromItemContent(
    modifier: Modifier = Modifier,
    state: ShareFromItemUiState,
    onEvent: (ShareFromItemEvent) -> Unit
) {
    if (state.isSingleSharingAvailable && state.isItemSharingAvailable) {
        ShareFromItemOptions(
            canUsePaidFeatures = state.canUsePaidFeatures,
            canShareVault = state.canShareVault,
            onEvent = onEvent
        )

        return
    }

    Column(
        modifier = modifier.padding(
            horizontal = PassTheme.dimens.bottomsheetHorizontalPadding,
            vertical = PassTheme.dimens.bottomsheetVerticalPadding
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
    ) {
        if (state.isSingleSharingAvailable) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.sharing_from_item_title),
                style = ProtonTheme.typography.defaultHighlightNorm,
                textAlign = TextAlign.Center
            )

            ShareItemSecureLinkRow(
                iconResId = CoreR.drawable.ic_proton_link,
                title = stringResource(id = R.string.share_with_secure_link_title),
                description = stringResource(id = R.string.share_with_secure_link_description),
                shouldShowPlusIcon = !state.canUsePaidFeatures,
                onClick = {
                    if (state.canUsePaidFeatures) {
                        ShareFromItemEvent.ShareSecureLink
                    } else {
                        ShareFromItemEvent.UpsellSecureLink
                    }.also(onEvent)
                }
            )

            if (state.canManageSharedVault || state.canViewSharedVaultMembers) {
                ShareItemSecureLinkRow(
                    iconResId = CoreR.drawable.ic_proton_users,
                    title = if (state.canManageSharedVault) {
                        R.string.share_with_manage_shared_vault_title
                    } else {
                        R.string.share_with_view_shared_vault_members_title
                    }.let { titleResId -> stringResource(id = titleResId) },
                    description = pluralStringResource(
                        id = R.plurals.share_with_manage_shared_vault_description,
                        count = state.sharedVaultMembersCount,
                        state.sharedVaultMembersCount
                    ),
                    shouldShowPlusIcon = false,
                    onClick = {
                        ShareFromItemEvent.ManageSharedVault
                            .also(onEvent)
                    }
                )

                // Under this conditions we don't need to show the rest of possible options nor divider
                return
            }
        }

        PassDivider(
            modifier = Modifier.padding(vertical = Spacing.small)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.sharing_from_item_title),
                style = ProtonTheme.typography.defaultHighlightNorm,
                textAlign = TextAlign.Center
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.sharing_from_item_description),
                style = PassTheme.typography.body3Weak(),
                color = PassTheme.colors.textWeak,
                textAlign = TextAlign.Center
            )
        }
    }

    if (state.vault is Some) {
        ShareThisVaultRow(
            vault = state.vault.value,
            onShareClick = { onEvent(ShareFromItemEvent.ShareVault) }
        )
    }

    if (state.showMoveToSharedVault) {
        ShareFromItemActionRow(
            modifier = Modifier.fillMaxWidth(),
            icon = CoreR.drawable.ic_proton_folder_arrow_in,
            title = R.string.sharing_from_item_move_to_shared_vault_action,
            onClick = { onEvent(ShareFromItemEvent.MoveToSharedVault) }
        )
    }

    when (state.showCreateVault) {
        CreateNewVaultState.Hide -> {}
        CreateNewVaultState.Allow -> {
            ShareFromItemActionRow(
                modifier = Modifier.fillMaxWidth(),
                icon = CoreR.drawable.ic_proton_plus,
                title = R.string.sharing_from_item_create_vault_to_share_action,
                onClick = { onEvent(ShareFromItemEvent.CreateNewVault) }
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.sharing_from_item_create_vault_to_share_subtitle),
                style = PassTheme.typography.body3Weak(),
                color = PassTheme.colors.textWeak,
                textAlign = TextAlign.Center
            )
        }

        CreateNewVaultState.Upgrade -> {
            ShareFromItemUpgradeRow(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEvent(ShareFromItemEvent.Upgrade) }
            )
        }

        CreateNewVaultState.VaultLimitReached -> {
            ShareFromItemVaultLimitReached(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
