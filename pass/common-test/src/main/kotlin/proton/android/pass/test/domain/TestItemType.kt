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

package proton.android.pass.test.domain

import proton.android.pass.account.fakes.TestKeyStoreCrypto
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.test.TestUtils

object TestItemType {

    fun login(
        email: String = TestUtils.randomString(),
        username: String = TestUtils.randomString(),
        password: String = TestUtils.randomString(),
        primaryTotp: String = "",
        websites: List<String> = emptyList(),
        packageInfoSet: Set<PackageInfo> = emptySet(),
        passkeys: List<Passkey> = emptyList()
    ): ItemType.Login = ItemType.Login(
        itemEmail = email,
        itemUsername = username,
        password = password,
        websites = websites,
        packageInfoSet = packageInfoSet,
        primaryTotp = TestKeyStoreCrypto.encrypt(primaryTotp),
        customFields = emptyList(),
        passkeys = passkeys
    )

    fun creditCard(
        cardNumber: String = TestUtils.randomString(),
        cardHolder: String = TestUtils.randomString(),
        expirationDate: String = TestUtils.randomString(),
        cvv: String = TestUtils.randomString(),
        pin: String = TestUtils.randomString()
    ): ItemType.CreditCard = ItemType.CreditCard(
        cardHolder = cardHolder,
        number = cardNumber,
        cvv = cvv,
        pin = pin,
        creditCardType = CreditCardType.MasterCard,
        expirationDate = expirationDate,
        customFields = emptyList()
    )
}
