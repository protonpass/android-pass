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

package proton.android.pass.domain.breach

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

@JvmInline
value class BreachId(val id: String)

@JvmInline
value class CustomEmailId(val id: String)

data class AliasEmailId(
    val shareId: ShareId,
    val itemId: ItemId
)

sealed interface BreachEmailId {

    val id: BreachId

    data class Custom(
        override val id: BreachId,
        val customEmailId: CustomEmailId
    ) : BreachEmailId

    data class Proton(
        override val id: BreachId,
        val addressId: AddressId
    ) : BreachEmailId

    data class Alias(
        override val id: BreachId,
        val shareId: ShareId,
        val itemId: ItemId
    ) : BreachEmailId
}

@Stable
data class BreachEmail(
    val emailId: BreachEmailId,
    val email: String,
    val severity: Double,
    val name: String,
    val createdAt: String,
    val publishedAt: String,
    val size: Long?,
    val passwordLastChars: String?,
    val exposedData: List<String>,
    val isResolved: Boolean,
    val actions: List<BreachAction>
)

@Serializable
data class BreachAction(
    val name: String,
    val code: BreachActionCode,
    val url: String?
)

enum class BreachActionCode(val code: String) {
    StayAlert("stay_alert"),
    PasswordSource("password_source"),
    PasswordExposed("password_exposed"),
    PasswordsAll("passwords_all"),
    Twofa("2fa"),
    Aliases("aliases");

    companion object {
        fun from(value: String): BreachActionCode = entries
            .firstOrNull { it.code == value }
            ?: StayAlert
    }
}
