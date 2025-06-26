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

package proton.android.pass.autofill.ui.autosave

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlinx.coroutines.Job
import proton.android.pass.commonui.api.onBottomSheetDismissed
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.features.auth.AUTH_GRAPH
import proton.android.pass.features.itemcreate.login.CreateLoginNavItem
import proton.android.pass.navigation.api.rememberAppNavigator
import proton.android.pass.navigation.api.rememberBottomSheetNavigator

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun AutosaveAppContent(
    modifier: Modifier = Modifier,
    arguments: AutoSaveArguments,
    needsAuth: Boolean,
    onNavigate: (AutosaveNavigation) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    val appNavigator = rememberAppNavigator(
        bottomSheetNavigator = rememberBottomSheetNavigator(bottomSheetState)
    )
    val bottomSheetJob: MutableState<Job?> = remember { mutableStateOf(null) }

    val startDestination = remember {
        if (needsAuth) AUTH_GRAPH else CreateLoginNavItem.route
    }
    PassModalBottomSheetLayout(bottomSheetNavigator = appNavigator.passBottomSheetNavigator) {
        NavHost(
            modifier = modifier.defaultMinSize(minHeight = 200.dp),
            navController = appNavigator.navController,
            startDestination = startDestination
        ) {
            autosaveActivityGraph(
                appNavigator = appNavigator,
                arguments = arguments,
                onNavigate = onNavigate,
                dismissBottomSheet = { block ->
                    onBottomSheetDismissed(
                        coroutineScope = coroutineScope,
                        modalBottomSheetState = bottomSheetState,
                        dismissJob = bottomSheetJob,
                        block = block
                    )
                }
            )
        }
    }
}
