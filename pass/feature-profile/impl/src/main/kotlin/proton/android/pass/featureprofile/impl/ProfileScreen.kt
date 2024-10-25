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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.commonui.api.AppUrls.PASS_STORE
import proton.android.pass.commonui.api.BrowserUtils.openWebsite
import proton.android.pass.domain.features.PaidFeature

@Suppress("CyclomaticComplexMethod", "ComplexMethod")
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    enterPinSuccess: Boolean,
    onNavigateEvent: (ProfileNavigation) -> Unit,
    onClearPinSuccess: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val newAccountReady by viewModel.onAccountReadyFlow.collectAsStateWithLifecycle(
        minActiveState = Lifecycle.State.CREATED,
        initialValue = emptySet()
    )
    LaunchedEffect(newAccountReady) {
        if (newAccountReady.isNotEmpty()) {
            viewModel.onNewAccountReady(newAccountReady)
            onNavigateEvent(ProfileNavigation.SyncDialog)
        }
    }
    val context = LocalContext.current

    LaunchedEffect(enterPinSuccess) {
        if (enterPinSuccess) {
            onClearPinSuccess()
            viewModel.onPinSuccessfullyEntered()
        }
    }
    LaunchedEffect(state.event) {
        when (state.event) {
            ProfileEvent.OpenFeatureFlags -> {
                onNavigateEvent(ProfileNavigation.FeatureFlags)
            }

            ProfileEvent.ConfigurePin -> {
                onNavigateEvent(ProfileNavigation.ConfigurePin)
            }

            ProfileEvent.Unknown -> {}
            ProfileEvent.AllMFA,
            ProfileEvent.HomeAliases,
            ProfileEvent.HomeCreditCards,
            ProfileEvent.HomeIdentities,
            ProfileEvent.HomeLogins,
            ProfileEvent.HomeNotes -> onNavigateEvent(ProfileNavigation.Home)
        }

        viewModel.onEventConsumed(state.event)
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
                ProfileUiEvent.OnHomeClick -> onNavigateEvent(ProfileNavigation.Home)
                ProfileUiEvent.OnRateAppClick -> openWebsite(context, PASS_STORE)
                ProfileUiEvent.OnSettingsClick -> onNavigateEvent(ProfileNavigation.Settings)
                ProfileUiEvent.OnUpgradeClick -> onNavigateEvent(ProfileNavigation.Upgrade)
                ProfileUiEvent.OnAppLockTypeClick -> onNavigateEvent(ProfileNavigation.AppLockType)
                ProfileUiEvent.OnAppLockTimeClick -> onNavigateEvent(ProfileNavigation.AppLockTime)
                is ProfileUiEvent.OnToggleBiometricSystemLock ->
                    viewModel.onToggleBiometricSystemLock(it.value)

                ProfileUiEvent.OnChangePinClick -> onNavigateEvent(ProfileNavigation.EnterPin)
                ProfileUiEvent.OnTutorialClick -> openWebsite(context, PASS_TUTORIAL)
                ProfileUiEvent.OnSecurityCenterClick -> onNavigateEvent(ProfileNavigation.SecurityCenter)
                ProfileUiEvent.OnSecureLinksClicked -> {
                    if (state.showUpgradeButton) {
                        ProfileNavigation.UpsellSecureLinks(PaidFeature.SecureLinks)
                    } else {
                        ProfileNavigation.SecureLinks
                    }.also(onNavigateEvent)
                }

                ProfileUiEvent.OnAliasesClicked -> {
                    if (state.isAdvancedAliasManagementEnabled) {
                        ProfileNavigation.AliasesSyncManagement
                    } else {
                        ProfileNavigation.AliasesSyncDetails
                    }.also(onNavigateEvent)
                }

                AccountSwitchEvent.OnAddAccount -> onNavigateEvent(ProfileNavigation.OnAddAccount)
                is AccountSwitchEvent.OnRemoveAccount -> onNavigateEvent(ProfileNavigation.OnRemoveAccount(it.userId))
                is AccountSwitchEvent.OnSignIn -> onNavigateEvent(ProfileNavigation.OnSignIn(it.userId))
                is AccountSwitchEvent.OnSignOut -> onNavigateEvent(ProfileNavigation.OnSignOut(it.userId))
                is AccountSwitchEvent.OnAccountSelected -> onNavigateEvent(ProfileNavigation.OnSwitchAccount(it.userId))
                is AccountSwitchEvent.OnManageAccount -> onNavigateEvent(ProfileNavigation.Account)
                ProfileUiEvent.OnAliasCountClick -> viewModel.onAliasCountClick()
                ProfileUiEvent.OnCreditCardCountClick -> viewModel.onCreditCardCountClick()
                ProfileUiEvent.OnIdentityCountClick -> viewModel.onIdentityCountClick()
                ProfileUiEvent.OnLoginCountClick -> viewModel.onLoginCountClick()
                ProfileUiEvent.OnMFACountClick -> viewModel.onMFACountClick()
                ProfileUiEvent.OnNoteCountClick -> viewModel.onNoteCountClick()
            }
        }
    )
}

object ProfileScreenTestTag {
    const val SCREEN = "ProfileScreenTestTag"
}

@VisibleForTesting
const val PASS_IMPORT = "https://proton.me/support/pass-import"

@VisibleForTesting
const val PASS_TUTORIAL = "https://www.youtube.com/watch?v=Nm4DCAjePOM"
