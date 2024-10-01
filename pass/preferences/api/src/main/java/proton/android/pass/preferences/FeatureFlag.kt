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
    SECURITY_CENTER_V1(
        title = "Security center (v1)",
        description = "Enable security center",
        key = "PassSentinelV1",
        isEnabledDefault = false
    ),
    IDENTITY_V1(
        title = "Identity (v1)",
        description = "Enable identity type",
        key = "PassIdentityV1",
        isEnabledDefault = false
    ),
    USERNAME_SPLIT(
        title = "Username split",
        description = "Enable split email/username",
        key = "PassUsernameSplit",
        isEnabledDefault = false
    ),
    ACCESS_KEY_V1(
        title = "Access key (v1)",
        description = "Enable access key",
        key = "PassAccessKeyV1",
        isEnabledDefault = false
    ),
    SECURE_LINK_V1(
        title = "Secure link (v1)",
        description = "Enable secure links",
        key = "PassPublicLinkV1",
        isEnabledDefault = false
    ),
    ACCOUNT_SWITCH_V1(
        title = "Account switch (v1)",
        description = "Enable account switch",
        key = "PassAccountSwitchV1",
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
    )
}
