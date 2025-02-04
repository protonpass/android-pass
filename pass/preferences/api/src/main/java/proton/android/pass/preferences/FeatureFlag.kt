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
    SL_ALIASES_SYNC(
        title = "SL aliases sync",
        description = "Enable SL aliases sync",
        key = "PassSimpleLoginAliasesSync",
        isEnabledDefault = false
    ),
    DIGITAL_ASSET_LINKS(
        title = "Digital asset links",
        description = "Enable Digital asset links",
        key = "PassDigitalAssetLinks",
        isEnabledDefault = false
    ),
    ADVANCED_ALIAS_MANAGEMENT_V1(
        title = "Alias management (v1)",
        description = "Enable advanced alias management",
        key = "PassAdvancedAliasManagementV1",
        isEnabledDefault = false
    ),
    ITEM_SHARING_V1(
        title = "Item sharing (v1)",
        description = "Enable single item sharing",
        key = "PassItemSharingV1",
        isEnabledDefault = false
    ),
    IN_APP_MESSAGES_V1(
        title = "In App Messages (v1)",
        description = "Enable in app messages",
        key = "PassInAppMessagesV1",
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
    SECURE_LINK_NEW_CRYPTO_V1(
        title = "Secure link new crypto (v1)",
        description = "Enable secure link new crypto",
        key = "PassSecureLinkCryptoChangeV1",
        isEnabledDefault = false
    ),
    NEW_LOGIN_FLOW(
        title = "New login flow",
        description = "Enable new login flow",
        key = null,
        isEnabledDefault = false
    )
}
