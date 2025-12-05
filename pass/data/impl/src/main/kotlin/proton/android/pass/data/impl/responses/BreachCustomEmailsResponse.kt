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
data class BreachesResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Breaches")
    val breaches: BreachesDetailsApiModel
)

@Serializable
data class BreachCustomEmailsResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Emails")
    val emails: BreachCustomEmailDetailsApiModel
)

@Serializable
data class BreachCustomEmailResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Email")
    val email: BreachCustomEmailApiModel
)

@Serializable
data class BreachesDetailsApiModel(
    @SerialName("EmailsCount")
    val emailsCount: Int,
    @SerialName("DomainsPeek")
    val domainPeeks: List<BreachDomainPeekApiModel>,
    @SerialName("CustomEmails")
    val customEmails: List<BreachCustomEmailApiModel>,
    @SerialName("Addresses")
    val protonEmails: List<BreachProtonEmailApiModel>,
    @SerialName("HasCustomDomains")
    val hasCustomDomains: Boolean
)

@Serializable
data class BreachDomainPeekApiModel(
    @SerialName("Domain")
    val domain: String,
    @SerialName("BreachTime")
    val breachTime: Long
)

@Serializable
data class BreachCustomEmailDetailsApiModel(
    @SerialName("CustomEmails")
    val customEmails: List<BreachCustomEmailApiModel>
)

@Serializable
data class BreachCustomEmailApiModel(
    @SerialName("CustomEmailID")
    val customEmailId: String,
    @SerialName("Email")
    val email: String,
    @SerialName("Verified")
    val verified: Boolean,
    @SerialName("BreachCounter")
    val breachCounter: Int,
    @SerialName("Flags")
    val flags: Int,
    @SerialName("LastBreachTime")
    val lastBreachTime: Int?
)

@Serializable
data class BreachProtonEmailApiModel(
    @SerialName("AddressID")
    val addressId: String,
    @SerialName("Email")
    val email: String,
    @SerialName("BreachCounter")
    val breachCounter: Int,
    @SerialName("Flags")
    val flags: Int,
    @SerialName("LastBreachTime")
    val lastBreachTime: Int?
)
