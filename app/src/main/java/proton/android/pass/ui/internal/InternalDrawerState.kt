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

package proton.android.pass.ui.internal

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.SwipeableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterialApi::class)
class InternalDrawerState(
    initialValue: InternalDrawerValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    val confirmStateChange: (InternalDrawerValue) -> Boolean = { true }
) : SwipeableState<InternalDrawerValue>(
    initialValue = initialValue,
    animationSpec = animationSpec,
    confirmStateChange = confirmStateChange
) {

    val isOpen: Boolean
        get() = currentValue != InternalDrawerValue.Closed

    suspend fun open() = animateTo(InternalDrawerValue.Open)

    suspend fun close() = animateTo(InternalDrawerValue.Closed)

    companion object {
        fun Saver(
            animationSpec: AnimationSpec<Float>,
            confirmStateChange: (InternalDrawerValue) -> Boolean
        ): Saver<InternalDrawerState, *> = Saver(
            save = { it.currentValue },
            restore = {
                InternalDrawerState(
                    initialValue = it,
                    animationSpec = animationSpec,
                    confirmStateChange = confirmStateChange
                )
            }
        )
    }
}

@Composable
fun rememberInternalDrawerState(
    initialValue: InternalDrawerValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    confirmStateChange: (InternalDrawerValue) -> Boolean = { true }
): InternalDrawerState {
    return rememberSaveable(saver = InternalDrawerState.Saver(animationSpec, confirmStateChange)) {
        InternalDrawerState(initialValue, animationSpec, confirmStateChange)
    }
}

enum class InternalDrawerValue {
    Open,
    Closed
}
