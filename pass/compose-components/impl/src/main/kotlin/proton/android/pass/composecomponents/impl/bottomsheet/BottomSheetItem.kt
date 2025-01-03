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

package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.runtime.Composable

interface BottomSheetItem {
    val title: @Composable () -> Unit
    val subtitle: @Composable (() -> Unit)?
    val leftIcon: @Composable (() -> Unit)?
    val endIcon: @Composable (() -> Unit)?
    val onClick: (() -> Unit)?
    val isDivider: Boolean
}

fun bottomSheetDivider(): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {}
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)?
        get() = null
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: (() -> Unit)?
        get() = null
    override val isDivider = true
}

fun List<BottomSheetItem>.withDividers(): List<BottomSheetItem> = this.flatMap { listOf(it, bottomSheetDivider()) }
    .dropLast(1)
