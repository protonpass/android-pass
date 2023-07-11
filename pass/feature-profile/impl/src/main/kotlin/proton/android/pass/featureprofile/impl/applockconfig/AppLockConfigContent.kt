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

package proton.android.pass.featureprofile.impl.applockconfig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.setting.SettingOption
import proton.android.pass.composecomponents.impl.setting.SettingToggle
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.featureprofile.impl.ProfileNavigation
import proton.android.pass.featureprofile.impl.R
import proton.android.pass.preferences.AppLockTimePreference
import proton.android.pass.preferences.AppLockTypePreference

@Composable
fun AppLockConfigContent(
    modifier: Modifier = Modifier,
    state: AppLockConfigUiState,
    onNavigateEvent: (ProfileNavigation) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(R.string.profile_option_advanced),
                onUpClick = { onNavigateEvent(ProfileNavigation.Back) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp)
                .roundedContainerNorm()
        ) {
            val typePreferenceTextId = when (state.appLockTypePreference) {
                AppLockTypePreference.Biometrics -> R.string.app_lock_config_biometric
                AppLockTypePreference.Pin -> R.string.app_lock_config_pin_code
            }
            SettingOption(
                label = stringResource(R.string.app_lock_config_unlock_with),
                text = stringResource(typePreferenceTextId),
                onClick = { onNavigateEvent(ProfileNavigation.AppLockType) }
            )
            PassDivider()
            val timePreferenceTextId = when (state.appLockTimePreference) {
                AppLockTimePreference.Immediately -> R.string.app_lock_immediately
                AppLockTimePreference.InOneMinute -> R.string.app_lock_one_minute
                AppLockTimePreference.InTwoMinutes -> R.string.app_lock_two_minutes
                AppLockTimePreference.InFiveMinutes -> R.string.app_lock_five_minutes
                AppLockTimePreference.InTenMinutes -> R.string.app_lock_ten_minutes
                AppLockTimePreference.InOneHour -> R.string.app_lock_one_hour
                AppLockTimePreference.InFourHours -> R.string.app_lock_four_hours
            }
            SettingOption(
                label = stringResource(R.string.app_lock_config_app_lock_time),
                text = stringResource(timePreferenceTextId),
                onClick = { onNavigateEvent(ProfileNavigation.AppLockTime) }
            )
            AnimatedVisibility(visible = true) {
                PassDivider()
                SettingToggle(
                    text = stringResource(R.string.app_lock_config_use_system_lock_when_biometric_fails),
                    isChecked = true,
                    onClick = { },
                )
            }
        }
    }
}

