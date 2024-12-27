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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.home.onboardingtips.OnBoardingTipPage.Autofill
import proton.android.pass.features.home.onboardingtips.OnBoardingTipPage.Invite
import proton.android.pass.features.home.onboardingtips.OnBoardingTipPage.NotificationPermission
import proton.android.pass.features.home.onboardingtips.OnBoardingTipPage.SLSync
import proton.android.pass.features.home.onboardingtips.OnBoardingTipPage.Trial

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OnBoardingTipContent(
    modifier: Modifier = Modifier,
    tipPage: OnBoardingTipPage,
    onClick: (OnBoardingTipPage) -> Unit,
    onDismiss: (OnBoardingTipPage) -> Unit
) {
    val scope = rememberCoroutineScope()
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = tipPage == Autofill,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(modifier = Modifier.padding(Spacing.medium)) {
                val dismissState = rememberDismissState(
                    confirmStateChange = {
                        if (it != DismissValue.Default) {
                            onDismiss(tipPage)
                        }
                        true
                    }
                )
                SwipeToDismiss(state = dismissState, background = {}) {
                    AutofillCard(
                        onClick = { onClick(tipPage) },
                        onDismiss = {
                            scope.launch {
                                dismissState.dismiss(DismissDirection.EndToStart)
                                onDismiss(tipPage)
                            }
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = tipPage == Trial,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(modifier = Modifier.padding(Spacing.medium)) {
                val dismissState = rememberDismissState(
                    confirmStateChange = {
                        if (it != DismissValue.Default) {
                            onDismiss(tipPage)
                        }
                        true
                    }
                )
                SwipeToDismiss(state = dismissState, background = {}) {
                    TrialCard(
                        onClick = { onClick(tipPage) },
                        onDismiss = {
                            scope.launch {
                                dismissState.dismiss(DismissDirection.EndToStart)
                                onDismiss(tipPage)
                            }
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = tipPage is SLSync,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(modifier = Modifier.padding(Spacing.medium)) {
                val dismissState = rememberDismissState(
                    confirmStateChange = {
                        if (it != DismissValue.Default) {
                            onDismiss(tipPage)
                        }
                        true
                    }
                )
                SwipeToDismiss(state = dismissState, background = {}) {
                    SLSyncCard(
                        aliasCount = (tipPage as? SLSync)?.aliasCount ?: 0,
                        onClick = { onClick(tipPage) },
                        onDismiss = {
                            scope.launch {
                                dismissState.dismiss(DismissDirection.EndToStart)
                                onDismiss(tipPage)
                            }
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = tipPage is Invite,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            (tipPage as? Invite)?.let { invite ->
                Box(modifier = Modifier.padding(all = Spacing.medium)) {
                    InviteCard(
                        pendingInvite = invite.pendingInvite,
                        onClick = { onClick(tipPage) }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = tipPage == NotificationPermission,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(modifier = Modifier.padding(Spacing.medium)) {
                val dismissState = rememberDismissState(
                    confirmStateChange = {
                        if (it != DismissValue.Default) {
                            onDismiss(tipPage)
                        }
                        true
                    }
                )
                SwipeToDismiss(state = dismissState, background = {}) {
                    NotificationPermissionCard(
                        onClick = { onClick(tipPage) },
                        onDismiss = {
                            scope.launch {
                                dismissState.dismiss(DismissDirection.EndToStart)
                                onDismiss(tipPage)
                            }
                        }
                    )
                }
            }
        }
    }
}
