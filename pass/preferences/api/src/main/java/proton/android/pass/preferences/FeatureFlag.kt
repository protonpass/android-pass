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

package proton.android.pass.preferences

enum class FeatureFlag(
    val title: String,
    val description: String,
    val isEnabledDefault: Boolean,
    val key: String? = null
) {
    AUTOFILL_DEBUG_MODE(
        title = "Autofill debug mode",
        description = "Enable autofill debug mode",
        key = null, // Cannot be activated server-side,
        isEnabledDefault = false
    ),
    EXTRA_LOGGING(
        title = "Extra logging",
        description = "Enable extra logging",
        key = "PassAndroidExtraLogging",
        isEnabledDefault = false
    ),
    RENAME_ADMIN_TO_MANAGER(
        title = "Rename Admin to Manager",
        description = "Enable Rename Admin to Manager",
        key = "PassRenameAdminToManager",
        isEnabledDefault = false
    ),
    PASS_HIDE_SHOW_VAULT(
        title = "Enable Vault visibility toggling",
        description = "Enable Vault visibility toggling",
        key = "PassHideShowVault",
        isEnabledDefault = false
    ),
    PASS_ALLOW_NO_VAULT(
        title = "Allow No Vault",
        description = "Allow a user to remove his last vault",
        key = "PassAllowNoVault",
        isEnabledDefault = false
    ),
    PASS_USER_EVENTS_V1(
        title = "User Events V1",
        description = "Enable user events",
        key = "PassUserEventsV1",
        isEnabledDefault = false
    ),
    PASS_GROUP_SHARE(
        title = "Enable group sharing",
        description = "Enable group sharing",
        key = "PassGroupInvitesV1",
        isEnabledDefault = false
    ),
    PASS_MOBILE_ON_BOARDING_V2(
        title = "Enable new OnBoarding",
        description = "Enable new OnBoarding",
        key = "PassMobileOnboardingV2",
        isEnabledDefault = false
    )
}
