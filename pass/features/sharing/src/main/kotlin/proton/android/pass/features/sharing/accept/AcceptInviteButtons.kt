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
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.domain.ShareType
import proton.android.pass.features.sharing.R

@Composable
internal fun AcceptInviteButtons(
    modifier: Modifier = Modifier,
    acceptInviteText: String,
    shareType: ShareType,
    progress: AcceptInviteProgress,
    onUiEvent: (AcceptInviteUiEvent) -> Unit
) {
    val isAcceptButtonLoading = remember(progress) {
        when (progress) {
            AcceptInviteProgress.Pending,
            AcceptInviteProgress.Rejecting -> false

            AcceptInviteProgress.Accepting,
            is AcceptInviteProgress.Downloading -> true
        }
    }

    val isRejectButtonLoading = remember(progress) {
        when (progress) {
            AcceptInviteProgress.Rejecting -> true

            AcceptInviteProgress.Pending,
            AcceptInviteProgress.Accepting,
            is AcceptInviteProgress.Downloading -> false
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        PassCircleButton(
            backgroundColor = PassTheme.colors.interactionNormMajor2,
            text = acceptInviteText,
            textColor = PassTheme.colors.interactionNormContrast,
            isLoading = isAcceptButtonLoading,
            onClick = {
                AcceptInviteUiEvent.OnAcceptInvitationClick(
                    shareType = shareType
                ).also(onUiEvent)
            }
        )

        if (progress is AcceptInviteProgress.Downloading) {
            AcceptInviteItemSyncStatus(
                downloaded = progress.downloaded,
                total = progress.total
            )
        } else {
            PassCircleButton(
                backgroundColor = PassTheme.colors.interactionNormMinor1,
                text = stringResource(id = R.string.sharing_reject_invitation),
                textColor = PassTheme.colors.interactionNormMajor1,
                isLoading = isRejectButtonLoading,
                onClick = { onUiEvent(AcceptInviteUiEvent.OnRejectInvitationClick) }
            )
        }
    }
}

@[Preview Composable]
internal fun AcceptInviteButtonsPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AcceptInviteButtons(
                acceptInviteText = "Accept invitation",
                progress = AcceptInviteProgress.Pending,
                shareType = ShareType.Vault,
                onUiEvent = {}
            )
        }
    }
}
