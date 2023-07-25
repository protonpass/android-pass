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

package proton.android.pass.featurehome.impl.onboardingtips

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
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.launch
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.AUTOFILL
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.INVITE
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.TRIAL

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OnBoardingTipContent(
    modifier: Modifier = Modifier,
    tipsSetToShow: ImmutableSet<OnBoardingTipPage>,
    onClick: (OnBoardingTipPage) -> Unit,
    onDismiss: (OnBoardingTipPage) -> Unit
) {
    val scope = rememberCoroutineScope()
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = tipsSetToShow.contains(AUTOFILL),
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(modifier = modifier.padding(16.dp)) {
                val dismissState = rememberDismissState(
                    confirmStateChange = {
                        if (it != DismissValue.Default) {
                            onDismiss(AUTOFILL)
                        }
                        true
                    }
                )
                SwipeToDismiss(state = dismissState, background = {}) {
                    AutofillCard(
                        onClick = { onClick(AUTOFILL) },
                        onDismiss = {
                            scope.launch {
                                dismissState.dismiss(DismissDirection.EndToStart)
                                onDismiss(AUTOFILL)
                            }
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = tipsSetToShow.contains(TRIAL),
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(modifier = modifier.padding(16.dp)) {
                val dismissState = rememberDismissState(
                    confirmStateChange = {
                        if (it != DismissValue.Default) {
                            onDismiss(TRIAL)
                        }
                        true
                    }
                )
                SwipeToDismiss(state = dismissState, background = {}) {
                    TrialCard(
                        onClick = { onClick(TRIAL) },
                        onDismiss = {
                            scope.launch {
                                dismissState.dismiss(DismissDirection.EndToStart)
                                onDismiss(TRIAL)
                            }
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = tipsSetToShow.contains(INVITE),
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Box(modifier = modifier.padding(16.dp)) {
                InviteCard(
                    onClick = { onClick(INVITE) }
                )
            }
        }
    }
}
