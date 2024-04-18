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

package proton.android.pass.securitycenter.api.passwords

import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.domain.Item

data class RepeatedPasswordsGroup(
    val password: EncryptedString,
    val items: List<Item>
) {
    val count: Int = items.size
}

data class RepeatedPasswordsReport(
    private val repeatedPasswords: Map<EncryptedString, List<Item>>
) {

    val repeatedPasswordsGroups: List<RepeatedPasswordsGroup> by lazy {
        repeatedPasswords.map { (password, items) ->
            RepeatedPasswordsGroup(password, items)
        }.sortedByDescending { it.count }
    }

    val repeatedPasswordsCount: Int = repeatedPasswords.size

}
