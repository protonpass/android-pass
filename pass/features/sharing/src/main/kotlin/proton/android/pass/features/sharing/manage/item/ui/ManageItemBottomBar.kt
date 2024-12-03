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

package proton.android.pass.features.sharing.manage.item.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.domain.Share
import proton.android.pass.features.sharing.R

@Composable
internal fun ManageItemBottomBar(
    modifier: Modifier = Modifier,
    share: Share,
    isLoading: Boolean,
    onUiEvent: (ManageItemUiEvent) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        if (share.isAdmin) {
            PassCircleButton(
                backgroundColor = PassTheme.colors.interactionNormMajor2,
                text = stringResource(id = R.string.share_manage_vault_share_with_more_people),
                textColor = PassTheme.colors.interactionNormContrast,
                onClick = {
                    ManageItemUiEvent.OnInviteShareClick(
                        shareId = share.id,
                        targetId = share.targetId
                    ).also(onUiEvent)
                }
            )
        }

        if (!share.isOwner) {
            PassCircleButton(
                backgroundColor = PassTheme.colors.interactionNormMinor1,
                text = stringResource(id = R.string.sharing_item_leave),
                textColor = PassTheme.colors.interactionNormMajor2,
                isLoading = isLoading,
                onClick = { onUiEvent(ManageItemUiEvent.OnLeaveShareClick) }
            )
        }
    }
}
