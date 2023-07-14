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

package proton.android.pass.featureprofile.impl

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.BrowserUtils.openWebsite

@Suppress("CyclomaticComplexMethod", "ComplexMethod")
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onNavigateEvent: (ProfileNavigation) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.event) {
        if (state.event == ProfileEvent.OpenFeatureFlags) {
            onNavigateEvent(ProfileNavigation.FeatureFlags)
            viewModel.clearEvent()
        }
    }

    ProfileContent(
        modifier = modifier,
        state = state,
        onEvent = {
            when (it) {
                ProfileUiEvent.OnAccountClick -> onNavigateEvent(ProfileNavigation.Account)
                ProfileUiEvent.OnAppVersionLongClick -> viewModel.onAppVersionLongClick()
                is ProfileUiEvent.OnAutofillClicked -> viewModel.onToggleAutofill(it.value)
                ProfileUiEvent.OnCopyAppVersionClick -> viewModel.copyAppVersion(state.appVersion)
                ProfileUiEvent.OnCreateItemClick -> onNavigateEvent(ProfileNavigation.CreateItem)
                ProfileUiEvent.OnFeedbackClick -> onNavigateEvent(ProfileNavigation.Feedback)
                ProfileUiEvent.OnImportExportClick -> openWebsite(context, PASS_IMPORT)
                ProfileUiEvent.OnListClick -> onNavigateEvent(ProfileNavigation.List)
                ProfileUiEvent.OnRateAppClick -> openWebsite(context, PASS_STORE)
                ProfileUiEvent.OnSettingsClick -> onNavigateEvent(ProfileNavigation.Settings)
                ProfileUiEvent.OnUpgradeClick -> onNavigateEvent(ProfileNavigation.Upgrade)
                ProfileUiEvent.OnAppLockTypeClick -> onNavigateEvent(ProfileNavigation.AppLockType)
                ProfileUiEvent.OnAppLockTimeClick -> onNavigateEvent(ProfileNavigation.AppLockTime)
                is ProfileUiEvent.OnToggleBiometricSystemLock -> viewModel.onToggleBiometricSystemLock(it.value)
            }
        }
    )
}

@VisibleForTesting
const val PASS_IMPORT = "https://proton.me/support/pass-import"

@VisibleForTesting
const val PASS_STORE = "https://play.google.com/store/apps/details?id=proton.android.pass"
