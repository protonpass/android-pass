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
data class BreachEmailsResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Breaches")
    val breachEmails: BreachEmails
)

@Serializable
data class BreachEmails(
    @SerialName("IsEligible")
    val isEligible: Boolean,
    @SerialName("Count")
    val count: Int,
    @SerialName("Breaches")
    val breaches: List<Breaches>
)

@Serializable
data class Breaches(
    @SerialName("ID")
    val id: String,
    @SerialName("Email")
    val email: String,
    @SerialName("ResolvedState")
    val resolvedState: Int,
    @SerialName("Severity")
    val severity: Double,
    @SerialName("Name")
    val name: String,
    @SerialName("CreatedAt")
    val createdAt: String,
    @SerialName("PublishedAt")
    val publishedAt: String,
    @SerialName("Source")
    val source: Source,
    @SerialName("Size")
    val size: Long?,
    @SerialName("ExposedData")
    val exposedData: List<DataExposed>,
    @SerialName("PasswordLastChars")
    val passwordLastChars: String?,
    @SerialName("Actions")
    val actions: List<Action>
)

@Serializable
data class Source(
    @SerialName("IsAggregated")
    val isAggregated: Boolean,
    @SerialName("Domain")
    val domain: String?,
    @SerialName("Category")
    val category: Category?,
    @SerialName("Country")
    val country: Country?
)

@Serializable
data class Category(
    @SerialName("Code")
    val code: String,
    @SerialName("Name")
    val name: String
)

@Serializable
data class Country(
    @SerialName("Code")
    val code: String,
    @SerialName("Name")
    val name: String,
    @SerialName("FlagEmoji")
    val flagEmoji: String
)

@Serializable
data class DataExposed(
    @SerialName("Code")
    val code: String,
    @SerialName("Name")
    val name: String
)

@Serializable
data class Action(
    @SerialName("Code")
    val code: String,
    @SerialName("Name")
    val name: String,
    @SerialName("Desc")
    val desc: String,
    @SerialName("Urls")
    val urls: List<String>?
)
