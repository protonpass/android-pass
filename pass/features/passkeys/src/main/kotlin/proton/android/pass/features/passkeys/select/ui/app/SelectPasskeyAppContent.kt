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

package proton.android.pass.features.passkeys.select.ui.app

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import proton.android.pass.features.passkeys.select.navigation.SelectPasskeyNavigation
import proton.android.pass.features.passkeys.select.navigation.selectPasskeyActivityGraph
import proton.android.pass.features.passkeys.select.presentation.SelectPasskeyActionAfterAuth
import proton.android.pass.features.passkeys.select.presentation.SelectPasskeyAppEvent
import proton.android.pass.features.passkeys.select.ui.bottomsheet.selectpasskey.SelectPasskeyBottomsheet
import proton.android.pass.features.selectitem.navigation.SelectItem
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
    domain: String,
    selectPasskey: SelectPasskeyAppEvent.SelectPasskeyFromItem?,
    actionAfterAuth: SelectPasskeyActionAfterAuth,
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
    val bottomSheetJob: MutableState<Job?> = remember { mutableStateOf(null) }

    PassModalBottomSheetLayout(bottomSheetNavigator = appNavigator.passBottomSheetNavigator) {
        NavHost(
            modifier = modifier.defaultMinSize(minHeight = 200.dp),
            navController = appNavigator.navController,
            startDestination = startDestination
        ) {
            selectPasskeyActivityGraph(
                appNavigator = appNavigator,
                domain = domain,
                actionAfterAuth = actionAfterAuth,
                onNavigate = onNavigate,
                onEvent = onEvent,
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
