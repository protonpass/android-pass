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

package proton.android.pass.composecomponents.impl.container

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AnimatedVisibilityState(initialState: Boolean) {
    val state = MutableTransitionState(initialState)

    fun toggle() {
        state.targetState = !state.targetState
    }
}

@Composable
fun rememberAnimatedVisibilityState(initialState: Boolean): AnimatedVisibilityState =
    remember {
        AnimatedVisibilityState(
            initialState = initialState,
        )
    }


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedVisibilityWithOnComplete(
    modifier: Modifier = Modifier,
    visibilityState: AnimatedVisibilityState,
    delayBeforeCallback: Long = DELAY_BEFORE_CALLBACK_MS,
    onComplete: () -> Unit,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    AnimatedVisibility(
        modifier = modifier,
        exit = fadeOut() + shrinkVertically(),
        visibleState = visibilityState.state,
    ) {
        if (!this.transition.isRunning && !visibilityState.state.targetState) {
            LaunchedEffect(Unit) {
                scope.launch {
                    delay(delayBeforeCallback)
                    onComplete()
                }
            }
        }
        content()
    }
}

private const val DELAY_BEFORE_CALLBACK_MS = 100L
