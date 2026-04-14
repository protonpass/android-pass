/*
 * Copyright (c) 2023-2026 Proton AG
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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.home.onboardingtips.OnBoardingTipPage.Invite

@Composable
fun OnBoardingTipContent(
    modifier: Modifier = Modifier,
    tipPage: OnBoardingTipPage,
    onClick: (OnBoardingTipPage) -> Unit,
    onDismiss: (OnBoardingTipPage) -> Unit
) {
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = tipPage is Invite,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            (tipPage as? Invite)?.let { invite ->
                Box(modifier = Modifier.padding(all = Spacing.medium)) {
                    InviteCard(
                        pendingInvite = invite.pendingInvite,
                        groupName = invite.groupName,
                        onClick = { onClick(tipPage) }
                    )
                }
            }
        }
    }
}
