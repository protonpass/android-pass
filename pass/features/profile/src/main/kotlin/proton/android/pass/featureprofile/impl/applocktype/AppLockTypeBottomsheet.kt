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

package proton.android.pass.featureprofile.impl.applocktype

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.featureprofile.impl.ProfileNavigation

@Composable
fun AppLockTypeBottomsheet(
    modifier: Modifier = Modifier,
    enterPinSuccess: Boolean,
    onNavigateEvent: (ProfileNavigation) -> Unit,
    viewModel: AppLockTypeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    if (enterPinSuccess) {
        LaunchedEffect(Unit) {
            viewModel.onPinSuccessfullyEntered(context.toClassHolder())
        }
    }
    LaunchedEffect(state.event) {
        when (state.event) {
            AppLockTypeEvent.ConfigurePin -> {
                onNavigateEvent(ProfileNavigation.ConfigurePin)
                viewModel.clearEvents()
            }
            AppLockTypeEvent.EnterPin -> {
                onNavigateEvent(ProfileNavigation.EnterPin)
                viewModel.clearEvents()
            }

            AppLockTypeEvent.Dismiss -> {
                onNavigateEvent(ProfileNavigation.CloseBottomSheet)
                viewModel.clearEvents()
            }

            AppLockTypeEvent.Unknown -> {}
        }
    }

    AppLockTypeBottomsheetContent(
        modifier = modifier.bottomSheet(),
        state = state
    ) { viewModel.onChanged(it, context.toClassHolder()) }
}
