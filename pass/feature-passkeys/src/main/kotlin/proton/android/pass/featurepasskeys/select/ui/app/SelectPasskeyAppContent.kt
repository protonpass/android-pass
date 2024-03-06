/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.featurepasskeys.select.ui.app

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlinx.coroutines.launch
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.bottomsheet.ProtonBottomSheetBackHandler
import proton.android.pass.featureauth.impl.AUTH_GRAPH
import proton.android.pass.featurepasskeys.select.navigation.SelectPasskeyNavigation
import proton.android.pass.featurepasskeys.select.navigation.selectPasskeyActivityGraph
import proton.android.pass.featurepasskeys.select.presentation.SelectPasskeyAppEvent
import proton.android.pass.featurepasskeys.select.ui.bottomsheet.selectpasskey.SelectPasskeyBottomsheet
import proton.android.pass.featureselectitem.navigation.SelectItem
import proton.android.pass.navigation.api.rememberAppNavigator
import proton.android.pass.navigation.api.rememberBottomSheetNavigator

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalMaterialNavigationApi::class
)
@Composable
fun SelectPasskeyAppContent(
    modifier: Modifier = Modifier,
    needsAuth: Boolean,
    selectPasskey: SelectPasskeyAppEvent.SelectPasskeyFromItem?,
    onEvent: (SelectPasskeyEvent) -> Unit,
    onNavigate: (SelectPasskeyNavigation) -> Unit
) {
    val startDestination = remember {
        if (needsAuth) {
            AUTH_GRAPH
        } else {
            SelectItem.route
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    ProtonBottomSheetBackHandler(
        bottomSheetState = bottomSheetState,
        coroutineScope = coroutineScope
    )

    val appNavigator = rememberAppNavigator(
        bottomSheetNavigator = rememberBottomSheetNavigator(bottomSheetState)
    )

    LaunchedEffect(selectPasskey) {
        selectPasskey?.let { item ->
            appNavigator.navigate(
                destination = SelectPasskeyBottomsheet,
                route = SelectPasskeyBottomsheet.buildRoute(
                    shareId = item.item.shareId,
                    itemId = item.item.id
                )
            )
        }
    }

    PassModalBottomSheetLayout(bottomSheetNavigator = appNavigator.passBottomSheetNavigator) {
        NavHost(
            modifier = modifier.defaultMinSize(minHeight = 200.dp),
            navController = appNavigator.navController,
            startDestination = startDestination
        ) {
            selectPasskeyActivityGraph(
                appNavigator = appNavigator,
                onNavigate = onNavigate,
                onEvent = onEvent,
                dismissBottomSheet = { callback ->
                    coroutineScope.launch {
                        bottomSheetState.hide()
                        callback()
                    }
                }
            )
        }
    }
}
