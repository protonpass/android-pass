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

/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// SOURCE CODE OBTAINED FROM: https://github.com/google/accompanist

package proton.android.pass.navigation.api

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.LocalOwnersProvider
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

@ExperimentalMaterialNavigationApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
@Suppress("MagicNumber", "SwallowedException")
internal fun SheetContentHost(
    columnHost: ColumnScope,
    backStackEntry: NavBackStackEntry?,
    sheetState: ModalBottomSheetState,
    saveableStateHolder: SaveableStateHolder,
    onSheetShown: (entry: NavBackStackEntry) -> Unit,
    onSheetDismissed: (entry: NavBackStackEntry) -> Unit,
) {
    val scope = rememberCoroutineScope()
    if (backStackEntry != null) {
        val currentOnSheetShown by rememberUpdatedState(onSheetShown)
        val currentOnSheetDismissed by rememberUpdatedState(onSheetDismissed)
        var hideCalled by remember(backStackEntry) { mutableStateOf(false) }
        LaunchedEffect(backStackEntry, hideCalled) {
            val sheetVisibility = snapshotFlow { sheetState.isVisible }
            sheetVisibility
                // We are only interested in changes in the sheet's visibility
                .distinctUntilChanged()
                // distinctUntilChanged emits the initial value which we don't need
                .drop(1)
                // We want to know when the sheet was visible but is not anymore
                .filter { isVisible -> !isVisible }
                // Finally, pop the back stack when the sheet has been hidden
                .collect { if (!hideCalled) currentOnSheetDismissed(backStackEntry) }
        }

        // We use this signal to know when its (almost) safe to `show` the bottom sheet
        // It will be set after the sheet's content has been `onGloballyPositioned`
        val contentPositionedSignal = remember(backStackEntry) {
            CompletableDeferred<Unit?>()
        }

        // Whenever the composable associated with the backStackEntry enters the composition, we
        // want to show the sheet, and hide it when this composable leaves the composition
        DisposableEffect(backStackEntry) {
            scope.launch {
                contentPositionedSignal.await()
                try {
                    // If we don't wait for a few frames before calling `show`, we will be too early
                    // and the sheet content won't have been laid out yet (even with our content
                    // positioned signal). If a sheet is tall enough to have a HALF_EXPANDED state,
                    // we might be here before the SwipeableState's anchors have been properly
                    // calculated, resulting in the sheet animating to the EXPANDED state when
                    // calling `show`. As a workaround, we wait for a magic number of frames.
                    // https://issuetracker.google.com/issues/200980998
                    repeat(AWAIT_FRAMES_BEFORE_SHOW) { awaitFrame() }
                    sheetState.show()
                } catch (sheetShowCancelled: CancellationException) {
                    // There is a race condition in ModalBottomSheetLayout that happens when the
                    // sheet content changes due to the anchors being re-calculated. This causes an
                    // animation to run for a short time, cancelling any currently running animation
                    // such as the one triggered by our `show` call.
                    // The sheet will still snap to the EXPANDED or HALF_EXPANDED state.
                    // In that case we want to wait until the sheet is visible. For safety, we only
                    // wait for 800 milliseconds - if the sheet is not visible until then, something
                    // has gone horribly wrong.
                    // https://issuetracker.google.com/issues/200980998
                    withTimeout(800) {
                        while (!sheetState.isVisible) {
                            awaitFrame()
                        }
                    }
                } finally {
                    // If, for some reason, the sheet is in a state where the animation is still
                    // running, there is a chance that it is already targeting the EXPANDED or
                    // HALF_EXPANDED state and will snap to that. In that case we can be fairly
                    // certain that the sheet will actually end up in that state.
                    if (sheetState.isVisible || sheetState.willBeVisible) {
                        currentOnSheetShown(backStackEntry)
                    }
                }
            }
            onDispose {
                scope.launch {
                    hideCalled = true
                    sheetState.internalHide()
                }
            }
        }

        val content = (backStackEntry.destination as PassBottomSheetNavigator.Destination).content
        backStackEntry.LocalOwnersProvider(saveableStateHolder) {
            Box(Modifier.onGloballyPositioned { contentPositionedSignal.complete(Unit) }) {
                columnHost.content(backStackEntry)
            }
        }
    } else {
        EmptySheet()
    }
}

@Composable
private fun EmptySheet() {
    // The swipeable modifier has a bug where it doesn't support having something with
    // height = 0
    // b/178529942
    // If there are no destinations on the back stack, we need to add something to work
    // around this
    Box(Modifier.height(1.dp))
}

private suspend fun awaitFrame() = withFrameNanos(onFrame = {})

/**
 * This magic number has been chosen through painful experiments.
 * - Waiting for 1 frame still results in the sheet fully expanding, which we don't want
 * - Waiting for 2 frames results in the `show` call getting cancelled
 * - Waiting for 3+ frames results in the sheet expanding to the correct state. Success!
 * We wait for a few frames more just to be sure.
 */
private const val AWAIT_FRAMES_BEFORE_SHOW = 3

// We have the same issue when we are hiding the sheet, but snapTo works better
@OptIn(ExperimentalMaterialApi::class)
private suspend fun ModalBottomSheetState.internalHide() {
    this.callPrivateSuspendFunc("snapTo", ModalBottomSheetValue.Hidden)
}

@OptIn(ExperimentalMaterialApi::class)
private val ModalBottomSheetState.willBeVisible: Boolean
    get() = targetValue == ModalBottomSheetValue.HalfExpanded || targetValue == ModalBottomSheetValue.Expanded

suspend inline fun <reified T> T.callPrivateSuspendFunc(name: String, vararg args: Any?): Any? =
    T::class
        .declaredMemberFunctions
        .firstOrNull { it.name == name }
        ?.apply { isAccessible = true }
        ?.callSuspend(this, *args)
