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

package proton.android.pass.features.extrapassword.presentation

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.log.api.PassLogger
import proton.android.pass.network.api.NetworkMonitor
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.notifications.api.SnackbarMessage
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@Stable
data class EnterExtraPasswordAppState(
    val snackbarMessage: Option<SnackbarMessage>,
    val theme: ThemePreference,
    val networkStatus: NetworkStatus
) {
    companion object {
        fun initial(theme: ThemePreference) = EnterExtraPasswordAppState(
            snackbarMessage = None,
            theme = theme,
            networkStatus = NetworkStatus.Online
        )
    }
}

@HiltViewModel
class EnterExtraPasswordAppViewModel @Inject constructor(
    private val preferenceRepository: UserPreferencesRepository,
    private val snackbarDispatcher: SnackbarDispatcher,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    private val themePreference: Flow<ThemePreference> = preferenceRepository
        .getThemePreference()
        .asResultWithoutLoading()
        .map(::getThemePreference)
        .onEach {
            when (it) {
                ThemePreference.Light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                ThemePreference.Dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                ThemePreference.System ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

    private val networkStatus: Flow<NetworkStatus> = networkMonitor
        .connectivity
        .distinctUntilChanged()


    val appUiState: StateFlow<EnterExtraPasswordAppState> = combine(
        snackbarDispatcher.snackbarMessage,
        themePreference,
        networkStatus
    ) { snackbarMessage, theme, networkStatus ->
        EnterExtraPasswordAppState(
            snackbarMessage = snackbarMessage,
            theme = theme,
            networkStatus = networkStatus
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = run {
            val theme = runBlocking {
                preferenceRepository.getThemePreference().first()
            }
            EnterExtraPasswordAppState.initial(theme)
        }
    )

    fun onSnackbarMessageDelivered() {
        viewModelScope.launch {
            snackbarDispatcher.snackbarMessageDelivered()
        }
    }

    private fun getThemePreference(state: LoadingResult<ThemePreference>): ThemePreference = when (state) {
        LoadingResult.Loading -> ThemePreference.System
        is LoadingResult.Success -> state.data
        is LoadingResult.Error -> {
            PassLogger.w(TAG, "Error getting ThemePreference")
            PassLogger.w(TAG, state.exception)
            ThemePreference.System
        }
    }
    companion object {
        private const val TAG = "EnterExtraPasswordAppViewModel"
    }
}
