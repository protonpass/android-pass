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

package proton.android.pass.autofill.debug

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.theme.SystemUIEffect
import proton.android.pass.composecomponents.impl.theme.isDark
import proton.android.pass.navigation.api.composable
import proton.android.pass.navigation.api.rememberAppNavigator

@Composable
fun AutofillDebugApp(
    modifier: Modifier = Modifier,
    viewModel: AutofillDebugAppViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val isDark = isDark(state.theme)
    SystemUIEffect(isDark = isDark)

    PassTheme(isDark = isDark) {
        ProvideWindowInsets {
            Scaffold(
                modifier = modifier
                    .fillMaxSize()
                    .background(PassTheme.colors.backgroundStrong)
                    .systemBarsPadding()
                    .imePadding(),
            ) { padding ->
                AutofillDebugAppContent(
                    modifier = Modifier.padding(padding),
                    sessions = state.sessions,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)
@Composable
private fun AutofillDebugAppContent(
    modifier: Modifier = Modifier,
    sessions: ImmutableList<AutofillSession>
) {
    val appNavigator = rememberAppNavigator()
    AnimatedNavHost(
        modifier = modifier.defaultMinSize(minHeight = 200.dp),
        navController = appNavigator.navController,
        startDestination = SessionsList.route
    ) {
        composable(SessionsList) {
            AutofillDebugSessions(
                sessions = sessions,
                onSessionClick = { session ->
                    appNavigator.navigate(
                        destination = SessionDetail,
                        route = SessionDetail.buildRoute(session.filename)
                    )
                }
            )
        }

        composable(SessionDetail) {
            SessionDetail(
                onBackClick = { appNavigator.onBackClick() }
            )
        }
    }
}
