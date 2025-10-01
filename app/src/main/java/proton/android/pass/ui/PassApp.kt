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

package proton.android.pass.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.LifecycleEffect
import proton.android.pass.commonui.api.PassTheme

@Composable
fun PassApp(
    modifier: Modifier = Modifier,
    supportPayment: Boolean,
    onNavigate: (AppNavigation) -> Unit,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val needsAuth by appViewModel.needsAuthState.collectAsStateWithLifecycle(
        minActiveState = Lifecycle.State.CREATED
    )
    val appUiState by appViewModel.appUiState.collectAsStateWithLifecycle()
    LifecycleEffect(
        onStop = { appViewModel.onStop() },
        onResume = { appViewModel.onResume() }
    )
    PassAppContent(
        modifier = modifier
            .background(color = PassTheme.colors.backgroundStrong)
            .imePadding(),
        appUiState = appUiState,
        needsAuth = needsAuth,
        onNavigate = {
            if (it is AppNavigation.Finish) {
                appViewModel.onStop()
            }
            onNavigate(it)
        },
        onSnackbarMessageDelivered = { appViewModel.onSnackbarMessageDelivered() },
        onCompleteUpdate = { appViewModel.onCompleteUpdate() },
        onInAppMessageBannerRead = { userId, id, key ->
            appViewModel.onInAppMessageBannerRead(userId, id, key)
        },
        onInAppMessageBannerDisplayed = { inAppMessageKey ->
            appViewModel.onInAppMessageBannerDisplayed(inAppMessageKey)
        },
        onInAppMessageBannerCTAClicked = { inAppMessageKey ->
            appViewModel.onInAppMessageBannerCTAClicked(inAppMessageKey)
        },
        supportPayment = supportPayment
    )
}
