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

package proton.android.pass.data.fakes.usecases.simplelogin

import proton.android.pass.domain.simplelogin.SimpleLoginAliasMailbox
import proton.android.pass.test.StringTestFactory.randomString
import kotlin.random.Random

internal object SimpleLoginAliasMailboxTestFactory {

    internal fun create(
        id: Long = Random.nextLong(),
        email: String = randomString(),
        pendingEmail: String = randomString(),
        isDefault: Boolean = Random.nextBoolean(),
        isVerified: Boolean = Random.nextBoolean(),
        aliasCount: Int = Random.nextInt()
    ): SimpleLoginAliasMailbox = SimpleLoginAliasMailbox(
        id = id,
        email = email,
        pendingEmail = pendingEmail,
        isDefault = isDefault,
        isVerified = isVerified,
        aliasCount = aliasCount
    )

}
