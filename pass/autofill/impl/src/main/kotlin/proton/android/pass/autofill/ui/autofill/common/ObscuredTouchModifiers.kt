/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.autofill.ui.autofill.common

import android.os.Build
import android.view.MotionEvent
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.consumeObscuredTouches(): Modifier = pointerInteropFilter { event ->
    val isObscured = event.flags and MotionEvent.FLAG_WINDOW_IS_OBSCURED != 0
    val isPartiallyObscured = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        event.flags and MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED != 0
    isObscured || isPartiallyObscured
}
