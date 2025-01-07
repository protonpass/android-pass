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

package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAccessResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Access")
    val accessResponse: AccessResponse
)

@Serializable
data class AccessResponse(
    @SerialName("Plan")
    val planResponse: PlanResponse,
    @SerialName("Monitor")
    val monitorResponse: MonitorResponse,
    @SerialName("PendingInvites")
    val pendingInvites: Int,
    @SerialName("WaitingNewUserInvites")
    val waitingNewUserInvites: Int,
    @SerialName("MinVersionUpgrade")
    val minVersionUpgrade: String?,
    @SerialName("UserData")
    val userData: UserDataResponse
)

@Serializable
data class MonitorResponse(
    @SerialName("ProtonAddress")
    val protonMonitorEnabled: Boolean,
    @SerialName("Aliases")
    val aliasMonitorEnabled: Boolean
)

@Serializable
data class PlanResponse(
    @SerialName("Type")
    val type: String,
    @SerialName("InternalName")
    val internalName: String,
    @SerialName("DisplayName")
    val displayName: String,
    @SerialName("VaultLimit")
    val vaultLimit: Int?,
    @SerialName("AliasLimit")
    val aliasLimit: Int?,
    @SerialName("TotpLimit")
    val totpLimit: Int?,
    @SerialName("HideUpgrade")
    val hideUpgrade: Boolean,
    @SerialName("TrialEnd")
    val trialEnd: Long?,
    @SerialName("ManageAlias")
    val manageAlias: Boolean,
    @SerialName("StorageAllowed")
    val storageAllowed: Boolean,
    @SerialName("StorageUsed")
    val storageUsed: Long,
    @SerialName("StorageQuota")
    val storageQuota: Long
)

@Serializable
data class UserDataResponse(
    @SerialName("DefaultShareID")
    val defaultShareID: String?,
    @SerialName("AliasSyncEnabled")
    val isAliasSyncEnabled: Boolean,
    @SerialName("PendingAliasToSync")
    val pendingAliasToSync: Int
)
