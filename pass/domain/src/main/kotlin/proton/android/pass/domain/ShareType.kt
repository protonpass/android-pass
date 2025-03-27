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

package proton.android.pass.domain

enum class ShareType(val value: Int) {
    Vault(1),
    Item(2);

    val isVaultShare: Boolean
        get() = this == Vault
    val isItemShare: Boolean
        get() = this == Item

    companion object {

        val map = entries.associateBy { it.value }

        fun from(value: Int): ShareType = when (value) {
            Vault.value -> Vault
            Item.value -> Item
            else -> throw IllegalArgumentException("Invalid share type value: $value")
        }

    }

}
