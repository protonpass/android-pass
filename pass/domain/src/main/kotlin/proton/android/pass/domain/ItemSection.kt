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

sealed interface ItemSection {

    data object CustomField : ItemSection

    @JvmInline
    value class ExtraSection(val index: Int) : ItemSection

    sealed interface Identity : ItemSection {

        data object Address : Identity

        data object Contact : Identity

        data object Personal : Identity

        data object Work : Identity
    }

    data object Login : ItemSection

    data object CreditCard : ItemSection

    data object WifiNetwork : ItemSection

    data object SSHKey : ItemSection
}
