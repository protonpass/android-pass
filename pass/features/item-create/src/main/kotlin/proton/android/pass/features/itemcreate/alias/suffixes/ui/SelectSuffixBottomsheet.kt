/*
 * Copyright (c) 2023-2025 Proton AG
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

package proton.android.pass.features.itemcreate.alias.suffixes.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.BrowserUtils.openWebsite
import proton.android.pass.features.itemcreate.alias.BaseAliasNavigation
import proton.android.pass.features.itemcreate.alias.suffixes.presentation.SelectSuffixEvent
import proton.android.pass.features.itemcreate.alias.suffixes.presentation.SelectSuffixViewModel

@Composable
fun SelectSuffixBottomsheet(
    modifier: Modifier = Modifier,
    viewModel: SelectSuffixViewModel = hiltViewModel(),
    onNavigate: (BaseAliasNavigation) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (state.event) {
            SelectSuffixEvent.DismissBottomsheet ->
                onNavigate(BaseAliasNavigation.CloseBottomsheet)

            SelectSuffixEvent.Idle -> {}
        }
        viewModel.onConsumeEvent(state.event)
    }
    SelectSuffixContent(
        modifier = modifier,
        state = state,
        onEvent = {
            when (it) {
                SelectSuffixUiEvent.AddCustomDomain -> {
                    viewModel.dismissFeatureDiscoveryBanner()
                    openWebsite(context, CUSTOM_DOMAIN_URL)
                }

                SelectSuffixUiEvent.DismissFeatureDiscoveryBanner ->
                    viewModel.dismissFeatureDiscoveryBanner()

                is SelectSuffixUiEvent.SelectSuffixUi ->
                    viewModel.selectSuffix(it.suffix)

                SelectSuffixUiEvent.Upgrade ->
                    onNavigate(BaseAliasNavigation.Upgrade)
            }
        }
    )
}

private const val CUSTOM_DOMAIN_URL = "https://pass.proton.me/settings#aliases"
