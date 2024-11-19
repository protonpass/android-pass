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

package proton.android.pass.features.sharing.accept

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultStrongNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.text.PassTextWithInnerStyle
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.InviteToken
import proton.android.pass.features.sharing.R

@Composable
internal fun AcceptInviteContentBody(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    inviteToken: InviteToken,
    inviterEmail: String,
    acceptInviteText: String,
    onUiEvent: (AcceptInviteUiEvent) -> Unit,
    infoContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(horizontal = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        Text.Headline(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            textAlign = TextAlign.Center
        )

        PassTextWithInnerStyle(
            modifier = Modifier.fillMaxWidth(),
            text = subtitle,
            textStyle = ProtonTheme.typography.defaultNorm,
            innerText = inviterEmail,
            innerStyle = ProtonTheme.typography.defaultStrongNorm,
            textAlign = TextAlign.Center
        )

        infoContent?.invoke()

        Column(
            verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            LoadingCircleButton(
                modifier = Modifier.fillMaxWidth(),
                color = PassTheme.colors.interactionNormMajor2,
                text = {
                    Text.Body1Regular(
                        text = acceptInviteText,
                        color = PassTheme.colors.interactionNormContrast
                    )
                },
                isLoading = false,
                onClick = {
                    AcceptInviteUiEvent.OnAcceptInvitationClick(
                        inviteToken = inviteToken
                    ).also(onUiEvent)
                }
            )

            LoadingCircleButton(
                modifier = Modifier.fillMaxWidth(),
                color = PassTheme.colors.interactionNormMinor1,
                text = {
                    Text.Body1Regular(
                        text = stringResource(id = R.string.sharing_reject_invitation),
                        color = PassTheme.colors.interactionNormMajor1
                    )
                },
                isLoading = false,
                onClick = {
                    AcceptInviteUiEvent.OnRejectInvitationClick(
                        inviteToken = inviteToken
                    ).also(onUiEvent)
                }
            )
        }
    }
}
