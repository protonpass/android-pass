/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.featureaccount.impl.accesskey.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import proton.android.pass.featureaccount.impl.AccountNavigation
import proton.android.pass.featureaccount.impl.accesskey.navigation.SetAccessKeyContentEvent
import proton.android.pass.featureaccount.impl.accesskey.presentation.SetAccessKeyViewModel

@Composable
fun SetAccessKeyScreen(
    modifier: Modifier = Modifier,
    viewModel: SetAccessKeyViewModel = hiltViewModel(),
    onNavigate: (AccountNavigation) -> Unit
) {
    SetAccessKeyContent(
        modifier = modifier,
        state = viewModel.getAccessKeyState(),
        onEvent = {
            when (it) {
                is SetAccessKeyContentEvent.Back -> onNavigate(AccountNavigation.Back)
                is SetAccessKeyContentEvent.OnAccessKeyRepeatValueChanged ->
                    viewModel.onAccessKeyRepeatValueChanged(it.value)

                is SetAccessKeyContentEvent.OnAccessKeyValueChanged ->
                    viewModel.onAccessKeyValueChanged(it.value)

                SetAccessKeyContentEvent.Submit -> {
                    viewModel.submit()
                }
            }
        }
    )
}

