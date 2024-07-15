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

package proton.android.pass.domain

enum class ItemDiffType {
    Content,
    Field,
    None
}

sealed interface ItemDiffs {

    val title: ItemDiffType

    val note: ItemDiffType

    data class Alias(
        override val title: ItemDiffType = ItemDiffType.None,
        override val note: ItemDiffType = ItemDiffType.None
    ) : ItemDiffs

    data class CreditCard(
        override val title: ItemDiffType = ItemDiffType.None,
        override val note: ItemDiffType = ItemDiffType.None
    ) : ItemDiffs

    data class Identity(
        override val title: ItemDiffType = ItemDiffType.None,
        override val note: ItemDiffType = ItemDiffType.None,
        val organization: ItemDiffType = ItemDiffType.None,
        val streetAddress: ItemDiffType = ItemDiffType.None,
        val floor: ItemDiffType = ItemDiffType.None,
        val city: ItemDiffType = ItemDiffType.None,
        val zipOrPostalCode: ItemDiffType = ItemDiffType.None,
        val stateOrProvince: ItemDiffType = ItemDiffType.None,
        val county: ItemDiffType = ItemDiffType.None,
        val countryOrRegion: ItemDiffType = ItemDiffType.None,
        val socialSecurityNumber: ItemDiffType = ItemDiffType.None,
        val passportNumber: ItemDiffType = ItemDiffType.None,
        val licenseNumber: ItemDiffType = ItemDiffType.None,
        val website: ItemDiffType = ItemDiffType.None,
        val secondPhoneNumber: ItemDiffType = ItemDiffType.None,
        val linkedin: ItemDiffType = ItemDiffType.None,
        val xHandle: ItemDiffType = ItemDiffType.None,
        val instagram: ItemDiffType = ItemDiffType.None,
        val facebook: ItemDiffType = ItemDiffType.None,
        val reddit: ItemDiffType = ItemDiffType.None,
        val yahoo: ItemDiffType = ItemDiffType.None,
        val firstName: ItemDiffType = ItemDiffType.None,
        val middleName: ItemDiffType = ItemDiffType.None,
        val lastName: ItemDiffType = ItemDiffType.None,
        val fullName: ItemDiffType = ItemDiffType.None,
        val email: ItemDiffType = ItemDiffType.None,
        val gender: ItemDiffType = ItemDiffType.None,
        val phoneNumber: ItemDiffType = ItemDiffType.None,
        val birthdate: ItemDiffType = ItemDiffType.None,
        val company: ItemDiffType = ItemDiffType.None,
        val jobTitle: ItemDiffType = ItemDiffType.None,
        val workPhoneNumber: ItemDiffType = ItemDiffType.None,
        val workEmail: ItemDiffType = ItemDiffType.None,
        val personalWebsite: ItemDiffType = ItemDiffType.None
    ) : ItemDiffs

    data class Login(
        override val title: ItemDiffType = ItemDiffType.None,
        override val note: ItemDiffType = ItemDiffType.None,
        val email: ItemDiffType = ItemDiffType.None,
        val username: ItemDiffType = ItemDiffType.None,
        val password: ItemDiffType = ItemDiffType.None,
        val totp: ItemDiffType = ItemDiffType.None
    ) : ItemDiffs

    data class Note(
        override val title: ItemDiffType = ItemDiffType.None,
        override val note: ItemDiffType = ItemDiffType.None
    ) : ItemDiffs

    data class Unknown(
        override val title: ItemDiffType = ItemDiffType.None,
        override val note: ItemDiffType = ItemDiffType.None
    ) : ItemDiffs

}
