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

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.contentColorFor
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.navigation.api.PassBottomSheetNavigator

@ExperimentalMaterialApi
@Composable
fun PassModalBottomSheetLayout(
    modifier: Modifier = Modifier,
    sheetContent: @Composable ColumnScope.() -> Unit,
    sheetState: ModalBottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    ),
    content: @Composable () -> Unit,
) {
    ModalBottomSheetLayout(
        modifier = modifier,
        sheetState = sheetState,
        sheetContent = sheetContent,
        sheetShape = PassTheme.shapes.bottomsheetShape,
        sheetBackgroundColor = PassTheme.colors.bottomSheetBackground,
        sheetContentColor = PassTheme.colors.bottomSheetBackground,
        scrimColor = PassTheme.colors.backdrop,
        content = content
    )
}

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)
@Composable
fun PassModalBottomSheetLayout(
    bottomSheetNavigator: PassBottomSheetNavigator,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ModalBottomSheetLayout(
        sheetState = bottomSheetNavigator.sheetState,
        sheetContent = bottomSheetNavigator.sheetContent,
        modifier = modifier,
        sheetShape = PassTheme.shapes.bottomsheetShape,
        sheetElevation = ModalBottomSheetDefaults.Elevation,
        sheetBackgroundColor = PassTheme.colors.backgroundWeak,
        sheetContentColor = contentColorFor(PassTheme.colors.backgroundWeak),
        scrimColor = PassTheme.colors.backdrop,
        content = content
    )
}
