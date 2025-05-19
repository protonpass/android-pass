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
    FILE_ATTACHMENTS_V1(
        title = "File attachments (v1)",
        description = "Enable file attachments",
        key = "PassFileAttachmentsV1",
        isEnabledDefault = false
    ),
    CUSTOM_TYPE_V1(
        title = "Enable custom types",
        description = "Enable custom types",
        key = "PassCustomTypeV1",
        isEnabledDefault = false
    ),
    FILE_ATTACHMENT_ENCRYPTION_V2(
        title = "File Attachment Encryption V2",
        description = "Enable File Attachment Encryption V2",
        key = "PassFileAttachmentEncryptionV2",
        isEnabledDefault = false
    ),
    RENAME_ADMIN_TO_MANAGER(
        title = "Rename Admin to Manager",
        description = "Enable Rename Admin to Manager",
        key = "PassRenameAdminToManager",
        isEnabledDefault = false
    )
}
