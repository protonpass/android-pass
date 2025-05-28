/*
 * Copyright (c) 2024-2025 Proton AG
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

package proton.android.pass.features.item.details.passkey.bottomsheet.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.features.item.details.passkey.bottomsheet.presentation.PasskeyDetailBottomSheetContent
import proton.android.pass.features.item.details.passkey.bottomsheet.presentation.PasskeyDetailBottomSheetEvent
import proton.android.pass.features.item.details.passkey.bottomsheet.presentation.PasskeyDetailBottomSheetViewModel
import proton.android.pass.features.item.details.shared.navigation.ItemDetailsNavDestination

@Composable
fun PasskeyDetailBottomSheet(
    modifier: Modifier = Modifier,
    onNavigated: (ItemDetailsNavDestination) -> Unit,
    viewModel: PasskeyDetailBottomSheetViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackHandler(onBack = { onNavigated(ItemDetailsNavDestination.DismissBottomSheet) })

    LaunchedEffect(state.event) {
        when (state.event) {
            PasskeyDetailBottomSheetEvent.Close -> onNavigated(ItemDetailsNavDestination.DismissBottomSheet)
            PasskeyDetailBottomSheetEvent.Idle -> {}
        }

        viewModel.clearEvent()
    }

    when (val content = state.content) {
        PasskeyDetailBottomSheetContent.Loading -> {
            Box(modifier = modifier.bottomSheet()) {
                CircularProgressIndicator(modifier = Modifier.size(Spacing.large))
            }
        }

        is PasskeyDetailBottomSheetContent.Success -> {
            PasskeyDetailBottomSheetContent(
                modifier = modifier,
                passkey = content.passkey
            )
        }
    }
}
