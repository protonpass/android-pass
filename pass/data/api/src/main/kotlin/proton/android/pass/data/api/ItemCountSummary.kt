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

package proton.android.pass.data.api

data class ItemCountSummary(
    val total: Long,
    val login: Long,
    val loginWithMFA: Long,
    val note: Long,
    val alias: Long,
    val creditCard: Long,
    val identities: Long,
    val sharedWithMe: Long,
    val sharedByMe: Long
) {

    val hasSharedWithMeItems: Boolean = sharedWithMe > 0

    val hasSharedByMeItems: Boolean = sharedByMe > 0

    companion object {

        val Initial = ItemCountSummary(
            total = 0,
            login = 0,
            loginWithMFA = 0,
            note = 0,
            alias = 0,
            creditCard = 0,
            identities = 0,
            sharedWithMe = 0,
            sharedByMe = 0
        )

    }

}
