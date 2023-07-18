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

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.common.api.combineN
import proton.android.pass.inappreview.api.InAppReviewManager
import proton.android.pass.inappupdates.api.InAppUpdatesManager
import proton.android.pass.log.api.PassLogger
import proton.android.pass.network.api.NetworkMonitor
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val preferenceRepository: UserPreferencesRepository,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val needsBiometricAuth: NeedsBiometricAuth,
    private val inAppUpdatesManager: InAppUpdatesManager,
    private val inAppReviewManager: InAppReviewManager,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val themePreference: Flow<ThemePreference> = preferenceRepository
        .getThemePreference()
        .asResultWithoutLoading()
        .map(::getThemePreference)
        .onEach {
            when (it) {
                ThemePreference.Light -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                ThemePreference.Dark -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                ThemePreference.System ->
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

    private val networkStatus: Flow<NetworkStatus> = networkMonitor
        .connectivity
        .distinctUntilChanged()

    val appUiState: StateFlow<AppUiState> = combineN(
        snackbarDispatcher.snackbarMessage,
        themePreference,
        networkStatus,
        needsBiometricAuth(),
        inAppUpdatesManager.observeInAppUpdateState(),
        inAppReviewManager.shouldRequestReview(),
        ::AppUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = run {
            val (theme, needsAuth) = runBlocking {
                preferenceRepository.getThemePreference()
                    .first() to needsBiometricAuth().first()
            }
            AppUiState.default(theme, needsAuth)
        }
    )

    fun onStop() = viewModelScope.launch {
        preferenceRepository.setHasAuthenticated(HasAuthenticated.NotAuthenticated)
        inAppUpdatesManager.completeUpdate()
    }

    fun onStart() = viewModelScope.launch {
        if (!appUiState.value.needsAuth) {
            preferenceRepository.setHasAuthenticated(HasAuthenticated.Authenticated)
        }
    }

    fun onResume() = viewModelScope.launch {
        inAppUpdatesManager.checkUpdateStalled()
    }

    fun onSnackbarMessageDelivered() {
        viewModelScope.launch {
            snackbarDispatcher.snackbarMessageDelivered()
        }
    }

    private fun getThemePreference(state: LoadingResult<ThemePreference>): ThemePreference =
        when (state) {
            LoadingResult.Loading -> ThemePreference.System
            is LoadingResult.Success -> state.data
            is LoadingResult.Error -> {
                PassLogger.w(TAG, state.exception, "Error getting ThemePreference")
                ThemePreference.System
            }
        }

    fun onCompleteUpdate() {
        inAppUpdatesManager.completeUpdate()
    }

    fun requestReview(activity: Activity) {
        inAppReviewManager.requestReview(activity)
    }

    companion object {
        private const val TAG = "AppViewModel"
    }
}
