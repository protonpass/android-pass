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

package proton.android.pass.features.extrapassword.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.messages.OfflineIndicator
import proton.android.pass.composecomponents.impl.messages.PassSnackbarHost
import proton.android.pass.composecomponents.impl.messages.rememberPassSnackbarHostState
import proton.android.pass.composecomponents.impl.snackbar.SnackBarLaunchedEffect
import proton.android.pass.composecomponents.impl.theme.SystemUIEffect
import proton.android.pass.composecomponents.impl.theme.isDark
import proton.android.pass.features.extrapassword.auth.presentation.EnterExtraPasswordAppViewModel
import proton.android.pass.network.api.NetworkStatus

@Composable
fun EnterExtraPasswordApp(
    modifier: Modifier = Modifier,
    userId: UserId,
    onSuccess: () -> Unit,
    onLogout: (UserId) -> Unit,
    appViewModel: EnterExtraPasswordAppViewModel = hiltViewModel()
) {
    val appUiState by appViewModel.appUiState.collectAsStateWithLifecycle()
    val isDark = isDark(appUiState.theme)
    SystemUIEffect(isDark = isDark)

    val scaffoldState = rememberScaffoldState()
    val passSnackbarHostState = rememberPassSnackbarHostState(scaffoldState.snackbarHostState)

    SnackBarLaunchedEffect(
        snackBarMessage = appUiState.snackbarMessage.value(),
        passSnackBarHostState = passSnackbarHostState,
        onSnackBarMessageDelivered = { appViewModel.onSnackbarMessageDelivered() }
    )

    PassTheme(isDark = isDark) {
        Scaffold(
            modifier = modifier
                .background(PassTheme.colors.backgroundStrong)
                .systemBarsPadding()
                .imePadding()
                .padding(),
            scaffoldState = scaffoldState,
            snackbarHost = { PassSnackbarHost(snackbarHostState = passSnackbarHostState) }
        ) { contentPadding ->
            Column(modifier = Modifier.padding(contentPadding)) {
                AnimatedVisibility(
                    visible = appUiState.networkStatus == NetworkStatus.Offline,
                    label = "EnterExtraPasswordApp-OfflineIndicator"
                ) {
                    OfflineIndicator()
                }
                EnterExtraPasswordScreen(
                    userId = userId,
                    onSuccess = onSuccess,
                    onLogout = onLogout
                )
            }
        }
    }
}
