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

package proton.android.pass.data.impl.db.mappers

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.proton.core.data.room.db.CommonConverters
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.data.impl.db.BreachTypeConverters
import proton.android.pass.data.impl.db.entities.BreachCustomEmailEntity
import proton.android.pass.data.impl.db.entities.BreachDomainPeekEntity
import proton.android.pass.data.impl.db.entities.BreachEmailEntity
import proton.android.pass.data.impl.db.entities.BreachProtonEmailEntity
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.breach.BreachCustomEmail
import proton.android.pass.domain.breach.BreachDomainPeek
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.domain.breach.BreachProtonEmail
import proton.android.pass.domain.breach.CustomEmailId

private val commonConverters = CommonConverters()
private val breachTypeConverters = BreachTypeConverters()

fun BreachCustomEmailEntity.toDomain(): BreachCustomEmail = BreachCustomEmail(
    id = CustomEmailId(customEmailId),
    email = email,
    verified = verified,
    breachCount = breachCount,
    flags = flags,
    lastBreachTime = lastBreachTime?.toInt()
)

fun BreachCustomEmail.toEntity(userId: String): BreachCustomEmailEntity = BreachCustomEmailEntity(
    userId = userId,
    customEmailId = id.id,
    email = email,
    verified = verified,
    breachCount = breachCount,
    flags = flags,
    lastBreachTime = lastBreachTime?.toLong()
)

fun BreachProtonEmailEntity.toDomain(): BreachProtonEmail = BreachProtonEmail(
    addressId = AddressId(addressId),
    email = email,
    breachCounter = breachCounter,
    flags = flags,
    lastBreachTime = lastBreachTime?.toInt()
)

fun BreachProtonEmail.toEntity(userId: String): BreachProtonEmailEntity = BreachProtonEmailEntity(
    userId = userId,
    addressId = addressId.id,
    email = email,
    breachCounter = breachCounter,
    flags = flags,
    lastBreachTime = lastBreachTime?.toLong()
)

fun BreachDomainPeekEntity.toDomain(): BreachDomainPeek = BreachDomainPeek(
    breachDomain = breachDomain,
    breachTime = breachTime
)

fun BreachDomainPeek.toEntity(userId: String): BreachDomainPeekEntity = BreachDomainPeekEntity(
    userId = userId,
    breachDomain = breachDomain,
    breachTime = breachTime
)

fun BreachEmailEntity.toDomain(): BreachEmail {
    val emailId = when (emailType) {
        BreachEmailEntity.EMAIL_TYPE_CUSTOM -> BreachEmailId.Custom(
            id = BreachId(breachId),
            customEmailId = CustomEmailId(emailOwnerId)
        )
        BreachEmailEntity.EMAIL_TYPE_PROTON -> BreachEmailId.Proton(
            id = BreachId(breachId),
            addressId = AddressId(emailOwnerId)
        )
        BreachEmailEntity.EMAIL_TYPE_ALIAS -> {
            val aliasShareId = shareId ?: throw IllegalArgumentException("shareId is null for Alias breach email")
            val aliasItemId = itemId ?: throw IllegalArgumentException("itemId is null for Alias breach email")
            BreachEmailId.Alias(
                id = BreachId(breachId),
                shareId = ShareId(aliasShareId),
                itemId = ItemId(aliasItemId)
            )
        }
        else -> throw IllegalArgumentException("Unknown email type: $emailType")
    }

    val exposedData = commonConverters.fromStringToListOfString(this.exposedData) ?: emptyList()
    val actions = breachTypeConverters.fromBreachActionList(this.actions) ?: emptyList()

    return BreachEmail(
        emailId = emailId,
        email = email,
        severity = severity,
        name = name,
        createdAt = createdAt,
        publishedAt = publishedAt,
        size = size,
        passwordLastChars = passwordLastChars,
        exposedData = exposedData,
        isResolved = isResolved,
        actions = actions
    )
}

fun BreachEmail.toEntity(userId: String): BreachEmailEntity {
    val (emailType, emailOwnerId, shareId, itemId) = when (val emailId = this.emailId) {
        is BreachEmailId.Custom -> Quadruple(
            BreachEmailEntity.EMAIL_TYPE_CUSTOM,
            emailId.customEmailId.id,
            null,
            null
        )
        is BreachEmailId.Proton -> Quadruple(
            BreachEmailEntity.EMAIL_TYPE_PROTON,
            emailId.addressId.id,
            null,
            null
        )
        is BreachEmailId.Alias -> Quadruple(
            BreachEmailEntity.EMAIL_TYPE_ALIAS,
            "${emailId.shareId.id}_${emailId.itemId.id}", // Composite key for emailOwnerId
            emailId.shareId.id,
            emailId.itemId.id
        )
    }

    val exposedDataString = commonConverters.fromListOfStringToString(exposedData)
        ?: Json.encodeToString(emptyList<String>())
    val actionsString = breachTypeConverters.toBreachActionList(actions)
        ?: Json.encodeToString(emptyList<String>())

    return BreachEmailEntity(
        breachId = emailId.id.id,
        userId = userId,
        emailType = emailType,
        emailOwnerId = emailOwnerId,
        shareId = shareId,
        itemId = itemId,
        email = email,
        severity = severity,
        name = name,
        createdAt = createdAt,
        publishedAt = publishedAt,
        size = size,
        passwordLastChars = passwordLastChars,
        exposedData = exposedDataString,
        isResolved = isResolved,
        actions = actionsString
    )
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

