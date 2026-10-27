/*
 * Copyright (c) 2023-2026 Proton AG
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
    RENAME_ADMIN_TO_MANAGER(
        title = "Rename Admin to Manager",
        description = "Enable Rename Admin to Manager",
        key = "PassRenameAdminToManager",
        isEnabledDefault = false
    ),
    ENABLE_PAGINATION(
        title = "Enable pagination",
        description = "Enable pagination on item list",
        key = "PassEnablePagination",
        isEnabledDefault = false
    ),
    PASS_EXPLORE_TAB(
        title = "Pass Explore tab",
        description = "Show the new Explore tab with cross-product promotion and Pass shortcuts",
        key = "PassExploreTab",
        isEnabledDefault = false
    ),
    PASS_PASSWORD_CHECKS(
        title = "Password checks",
        description = "Show the password requirement checks while editing or generating a password",
        key = "PassPasswordChecks",
        isEnabledDefault = false
    ),
    PASS_MONITOR_PER_CHECK_EXCLUSION(
        title = "Monitor per check exclusion",
        description = "Allow the user to exclude an item from a subset of Pass Monitor checks",
        key = "PassMonitorPerCheckExclusion",
        isEnabledDefault = false
    ),
    PASS_OFFLINE_ATTACHMENTS(
        title = "Offline attachments",
        description = "Enable offline attachment downloads",
        key = "PassOfflineAttachments",
        isEnabledDefault = false
    ),
    PASS_FORCE_SYNC_FOLDERS(
        title = "Force sync folders",
        description = "Force a one-off full sync for users whose folders were lost by a pre-folders build",
        key = "PassForceSyncFolders",
        isEnabledDefault = false
    ),
    PASS_FOLDER_NAVIGATION(
        title = "Folder navigation",
        description = "Show the new folder navigation in vault and folder item lists",
        key = "PassFolderNavigation",
        isEnabledDefault = false
    )
}
