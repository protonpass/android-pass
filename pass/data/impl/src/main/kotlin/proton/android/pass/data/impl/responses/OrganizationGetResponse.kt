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

package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrganizationGetResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Organization")
    val organization: OrganizationGetOrganization?
)

@Serializable
data class OrganizationGetOrganization(
    @SerialName("CanUpdate")
    val canUpdate: Boolean,
    @SerialName("Settings")
    val settings: OrganizationGetOrganizationSettings?
)

@Serializable
data class OrganizationGetOrganizationSettings(
    @SerialName("ShareMode")
    val shareMode: Int,
    @SerialName("ForceLockSeconds")
    val forceLockSeconds: Int,
    @SerialName("PasswordPolicy")
    val passwordPolicy: OrganizationGetOrganizationSettingsPasswordPolicy?
)

@Serializable
data class OrganizationGetOrganizationSettingsPasswordPolicy(
    @SerialName("RandomPasswordAllowed")
    val randomPasswordAllowed: Boolean,
    @SerialName("RandomPasswordMinLength")
    val randomPasswordMinLength: Int?,
    @SerialName("RandomPasswordMaxLength")
    val randomPasswordMaxLength: Int?,
    @SerialName("RandomPasswordMustIncludeNumbers")
    val randomPasswordMustIncludeNumbers: Boolean?,
    @SerialName("RandomPasswordMustIncludeSymbols")
    val randomPasswordMustIncludeSymbols: Boolean?,
    @SerialName("RandomPasswordMustIncludeUppercase")
    val randomPasswordMustIncludeUppercase: Boolean?,
    @SerialName("MemorablePasswordAllowed")
    val memorablePasswordAllowed: Boolean,
    @SerialName("MemorablePasswordMinWords")
    val memorablePasswordMinWords: Int?,
    @SerialName("MemorablePasswordMaxWords")
    val memorablePasswordMaxWords: Int?,
    @SerialName("MemorablePasswordMustCapitalize")
    val memorablePasswordMustCapitalize: Boolean?,
    @SerialName("MemorablePasswordMustIncludeNumbers")
    val memorablePasswordMustIncludeNumbers: Boolean?,
    @SerialName("MemorablePasswordMustIncludeSeparator")
    val memorablePasswordMustIncludeSeparator: Boolean?
)
