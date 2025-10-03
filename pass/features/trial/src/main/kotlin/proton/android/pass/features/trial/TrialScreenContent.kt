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

package proton.android.pass.features.trial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.topbar.iconbutton.CrossBackIconButton

@Composable
fun TrialScreenContent(
    modifier: Modifier = Modifier,
    state: TrialUiState,
    onNavigate: (TrialNavigation) -> Unit,
    onLearnMore: () -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(PassTheme.colors.backgroundStrong)
            .systemBarsPadding(),
        topBar = {
            ProtonTopAppBar(
                backgroundColor = PassTheme.colors.itemDetailBackground,
                title = { },
                navigationIcon = {
                    CrossBackIconButton(
                        onUpClick = { onNavigate(TrialNavigation.CloseScreen) }
                    )
                }
            )
        }
    ) { padding ->
        TrialContent(
            modifier = Modifier.padding(padding),
            state = state,
            onNavigate = onNavigate,
            onLearnMore = onLearnMore
        )
    }
}
