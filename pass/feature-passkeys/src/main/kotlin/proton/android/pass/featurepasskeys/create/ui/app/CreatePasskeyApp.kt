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

package proton.android.pass.featurepasskeys.create.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.theme.SystemUIEffect
import proton.android.pass.composecomponents.impl.theme.isDark
import proton.android.pass.featurepasskeys.create.presentation.CreatePasskeyAppState
import proton.android.pass.featurepasskeys.create.presentation.CreatePasskeyAppViewModel
import proton.android.pass.featurepasskeys.create.presentation.CreatePasskeyRequest

@Composable
fun CreatePasskeyApp(
    modifier: Modifier = Modifier,
    appState: CreatePasskeyAppState.Ready,
    request: CreatePasskeyRequest,
    onNavigate: (CreatePasskeyNavigation) -> Unit,
    viewModel: CreatePasskeyAppViewModel = hiltViewModel()
) {
    val isDark = isDark(appState.theme)
    SystemUIEffect(isDark = isDark)

    PassTheme(isDark = isDark) {
        Scaffold(
            modifier = modifier
                .background(PassTheme.colors.backgroundStrong)
                .systemBarsPadding()
                .imePadding()
        ) { padding ->
            CreatePasskeyAppContent(
                modifier = Modifier.padding(padding),
                needsAuth = appState.needsAuth,
                request = request,
                onNavigate = onNavigate
            )
        }
    }
}
