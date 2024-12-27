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

package proton.android.pass.features.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.setting.ColorSettingOption
import proton.android.pass.composecomponents.impl.setting.SettingOption
import proton.android.pass.composecomponents.impl.setting.SettingToggle
import proton.android.pass.features.profile.EnforcedAppLockSectionState.Password
import proton.android.pass.preferences.AppLockTimePreference
import proton.android.pass.preferences.value
import me.proton.core.presentation.compose.R as CoreR

@Composable
internal fun AppLockSection(
    modifier: Modifier = Modifier,
    appLockSectionState: AppLockSectionState,
    onEvent: (ProfileUiEvent) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
        Text(
            text = stringResource(R.string.profile_security),
            style = ProtonTheme.typography.defaultSmallWeak
        )
        Column(
            modifier = Modifier.roundedContainerNorm()
        ) {
            val unlockMethodLabel = if (appLockSectionState is UserAppLockSectionState.None) {
                stringResource(R.string.profile_option_lock_with)
            } else {
                stringResource(R.string.profile_option_unlock_with)
            }

            val unlockMethodValue = when (appLockSectionState) {
                is UserAppLockSectionState.Biometric, is EnforcedAppLockSectionState.Biometric ->
                    stringResource(id = R.string.app_lock_config_biometric)

                is UserAppLockSectionState.None ->
                    stringResource(id = R.string.app_lock_config_none)

                is UserAppLockSectionState.Pin, is EnforcedAppLockSectionState.Pin ->
                    stringResource(id = R.string.app_lock_config_pin_code)

                is Password ->
                    stringResource(id = R.string.app_lock_config_password)

                else -> ""
            }
            SettingOption(
                text = unlockMethodValue,
                label = unlockMethodLabel,
                isLoading = appLockSectionState is AppLockSectionState.Loading,
                onClick = { onEvent(ProfileUiEvent.OnAppLockTypeClick) }
            )

            AnimatedVisibility(visible = appLockSectionState is Password) {
                if (appLockSectionState is Password) {
                    PassDivider()
                    SettingOption(
                        label = stringResource(R.string.app_lock_config_app_lock_time),
                        text = secondsToText(seconds = appLockSectionState.seconds)
                    )
                }
            }

            AnimatedVisibility(visible = appLockSectionState is BiometricSection) {
                Column {
                    PassDivider()
                    when (appLockSectionState) {
                        is UserAppLockSectionState.Biometric -> {
                            SettingOption(
                                label = stringResource(R.string.app_lock_config_app_lock_time),
                                text = getAppLockTimePreferenceText(appLockSectionState.appLockTimePreference),
                                onClick = { onEvent(ProfileUiEvent.OnAppLockTimeClick) }
                            )
                            PassDivider()
                        }

                        is EnforcedAppLockSectionState.Biometric -> {
                            SettingOption(
                                label = stringResource(R.string.app_lock_config_app_lock_time),
                                text = secondsToText(seconds = appLockSectionState.seconds)
                            )
                            PassDivider()
                        }

                        else -> {}
                    }

                    SettingToggle(
                        text = stringResource(R.string.app_lock_config_use_system_lock_when_biometric_fails),
                        isChecked = (appLockSectionState as? BiometricSection)
                            ?.biometricSystemLockPreference
                            ?.value()
                            ?: false,
                        onClick = { onEvent(ProfileUiEvent.OnToggleBiometricSystemLock(it)) }
                    )
                }
            }
            AnimatedVisibility(visible = appLockSectionState is PinSection) {
                Column {
                    PassDivider()
                    when (appLockSectionState) {
                        is UserAppLockSectionState.Pin -> {
                            SettingOption(
                                label = stringResource(R.string.app_lock_config_app_lock_time),
                                text = getAppLockTimePreferenceText(appLockSectionState.appLockTimePreference),
                                onClick = { onEvent(ProfileUiEvent.OnAppLockTimeClick) }
                            )
                            PassDivider()
                        }

                        is EnforcedAppLockSectionState.Pin -> {
                            SettingOption(
                                label = stringResource(R.string.app_lock_config_app_lock_time),
                                text = secondsToText(seconds = appLockSectionState.seconds)
                            )
                            PassDivider()
                        }

                        else -> {}
                    }

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
private fun secondsToText(seconds: Int): String {
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> pluralStringResource(R.plurals.profile_after_days, days, days)
        hours > 0 -> pluralStringResource(R.plurals.profile_after_hours, hours, hours)
        minutes > 0 -> pluralStringResource(R.plurals.profile_after_minutes, minutes, minutes)
        else -> stringResource(R.string.profile_less_than_a_minute)
    }
}

@Composable
private fun getAppLockTimePreferenceText(appLockTimePreference: AppLockTimePreference) = when (appLockTimePreference) {
    AppLockTimePreference.Immediately -> stringResource(R.string.app_lock_immediately)
    AppLockTimePreference.InOneMinute -> stringResource(R.string.app_lock_one_minute)
    AppLockTimePreference.InTwoMinutes -> stringResource(R.string.app_lock_two_minutes)
    AppLockTimePreference.InFiveMinutes -> stringResource(R.string.app_lock_five_minutes)
    AppLockTimePreference.InTenMinutes -> stringResource(R.string.app_lock_ten_minutes)
    AppLockTimePreference.InOneHour -> stringResource(R.string.app_lock_one_hour)
    AppLockTimePreference.InFourHours -> stringResource(R.string.app_lock_four_hours)
}


internal class ThemeAndAppLockSectionPreviewProvider :
    ThemePairPreviewProvider<AppLockSectionState>(AppLockSectionStatePreviewProvider())

@Preview
@Composable
internal fun AppLockSectionPreview(
    @PreviewParameter(ThemeAndAppLockSectionPreviewProvider::class) input: Pair<Boolean, AppLockSectionState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AppLockSection(appLockSectionState = input.second, onEvent = {})
        }
    }
}
