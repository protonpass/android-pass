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

package proton.android.pass.features.profile.applocktime

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.features.profile.R
import proton.android.pass.preferences.AppLockTimePreference

@Composable
fun AppLockTimeBottomsheetContent(
    modifier: Modifier = Modifier,
    state: AppLockTimeUiState,
    onSelected: (AppLockTimePreference) -> Unit
) {
    BottomSheetItemList(
        modifier = modifier,
        items = state.items.map { preference ->
            appLockBottomSheetItem(
                preference = preference,
                isSelected = preference == state.selected,
                onClick = { onSelected(preference) }
            )
        }.withDividers().toPersistentList()
    )
}

private fun appLockBottomSheetItem(
    preference: AppLockTimePreference,
    isSelected: Boolean,
    onClick: () -> Unit
): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {
            val title = when (preference) {
                AppLockTimePreference.Immediately -> R.string.app_lock_immediately
                AppLockTimePreference.InOneMinute -> R.string.app_lock_one_minute
                AppLockTimePreference.InTwoMinutes -> R.string.app_lock_two_minutes
                AppLockTimePreference.InFiveMinutes -> R.string.app_lock_five_minutes
                AppLockTimePreference.InTenMinutes -> R.string.app_lock_ten_minutes
                AppLockTimePreference.InOneHour -> R.string.app_lock_one_hour
                AppLockTimePreference.InFourHours -> R.string.app_lock_four_hours
            }
            BottomSheetItemTitle(text = stringResource(title))
        }
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)?
        get() = null
    override val endIcon: (@Composable () -> Unit)?
        get() = if (isSelected) {
            {
                BottomSheetItemIcon(
                    iconId = me.proton.core.presentation.R.drawable.ic_proton_checkmark,
                    tint = PassTheme.colors.loginInteractionNormMajor1
                )
            }

        } else null
    override val onClick: () -> Unit
        get() = onClick
    override val isDivider = false
}
