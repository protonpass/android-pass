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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.setting.ColorSettingOption
import proton.android.pass.composecomponents.impl.setting.SettingOption
import proton.android.pass.composecomponents.impl.setting.SettingToggle
import proton.android.pass.preferences.AppLockTimePreference
import proton.android.pass.preferences.value
import me.proton.core.presentation.compose.R as CoreR

@Composable
fun AppLockSection(
    modifier: Modifier = Modifier,
    appLockSectionState: AppLockSectionState,
    onEvent: (ProfileUiEvent) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.profile_security),
            style = ProtonTheme.typography.defaultSmallWeak
        )
        Column(
            modifier = Modifier.roundedContainerNorm(),
        ) {
            val label = if (appLockSectionState is AppLockSectionState.None) {
                stringResource(R.string.profile_option_lock_with)
            } else {
                stringResource(R.string.profile_option_unlock_with)
            }
            val text = when (appLockSectionState) {
                is AppLockSectionState.Biometric -> stringResource(id = R.string.app_lock_config_biometric)
                AppLockSectionState.None -> stringResource(id = R.string.app_lock_config_none)
                is AppLockSectionState.Pin -> stringResource(id = R.string.app_lock_config_pin_code)
            }
            SettingOption(
                text = text,
                label = label,
                onClick = { onEvent(ProfileUiEvent.OnAppLockTypeClick) }
            )
            AnimatedVisibility(visible = appLockSectionState is AppLockSectionState.Biometric) {
                Column {
                    val biometricState = appLockSectionState as? AppLockSectionState.Biometric
                    PassDivider()
                    val timePreferenceText = biometricState
                        ?.let { getAppLockTimePreferenceText(biometricState.appLockTimePreference) }
                        ?: ""

                    SettingOption(
                        label = stringResource(R.string.app_lock_config_app_lock_time),
                        text = timePreferenceText,
                        onClick = { onEvent(ProfileUiEvent.OnAppLockTimeClick) }
                    )
                    PassDivider()
                    SettingToggle(
                        text = stringResource(R.string.app_lock_config_use_system_lock_when_biometric_fails),
                        isChecked = biometricState?.biometricSystemLockPreference?.value() ?: false,
                        onClick = { onEvent(ProfileUiEvent.OnToggleBiometricSystemLock(it)) }
                    )
                }
            }
            AnimatedVisibility(visible = appLockSectionState is AppLockSectionState.Pin) {
                Column {
                    val pinState = appLockSectionState as? AppLockSectionState.Pin

                    PassDivider()
                    val timePreferenceText = pinState
                        ?.let { getAppLockTimePreferenceText(pinState.appLockTimePreference) }
                        ?: ""
                    SettingOption(
                        label = stringResource(R.string.app_lock_config_app_lock_time),
                        text = timePreferenceText,
                        onClick = { onEvent(ProfileUiEvent.OnAppLockTimeClick) }
                    )
                    PassDivider()
                    ColorSettingOption(
                        text = stringResource(R.string.profile_option_change_pin_code),
                        textColor = PassTheme.colors.interactionNormMajor2,
                        iconBgColor = PassTheme.colors.interactionNormMinor1,
                        icon = {
                            Icon(
                                painter = painterResource(CoreR.drawable.ic_proton_grid_3),
                                contentDescription = "",
                                tint = PassTheme.colors.interactionNormMajor2
                            )
                        },
                        onClick = { onEvent(ProfileUiEvent.OnChangePinClick) }
                    )
                }
            }
        }
    }
}

@Composable
private fun getAppLockTimePreferenceText(appLockTimePreference: AppLockTimePreference) =
    when (appLockTimePreference) {
        AppLockTimePreference.Immediately -> stringResource(R.string.app_lock_immediately)
        AppLockTimePreference.InOneMinute -> stringResource(R.string.app_lock_one_minute)
        AppLockTimePreference.InTwoMinutes -> stringResource(R.string.app_lock_two_minutes)
        AppLockTimePreference.InFiveMinutes -> stringResource(R.string.app_lock_five_minutes)
        AppLockTimePreference.InTenMinutes -> stringResource(R.string.app_lock_ten_minutes)
        AppLockTimePreference.InOneHour -> stringResource(R.string.app_lock_one_hour)
        AppLockTimePreference.InFourHours -> stringResource(R.string.app_lock_four_hours)
    }


class ThemeAndAppLockSectionPreviewProvider :
    ThemePairPreviewProvider<AppLockSectionState>(AppLockSectionStatePreviewProvider())

@Preview
@Composable
fun AppLockSectionPreview(
    @PreviewParameter(ThemeAndAppLockSectionPreviewProvider::class) input: Pair<Boolean, AppLockSectionState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AppLockSection(appLockSectionState = input.second, onEvent = {})
        }
    }
}
