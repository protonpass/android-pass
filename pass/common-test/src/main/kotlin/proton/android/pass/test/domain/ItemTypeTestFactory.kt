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

import proton.android.pass.account.fakes.FakeKeyStoreCrypto
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.test.StringTestFactory

object ItemTypeTestFactory {

    fun login(
        email: String = StringTestFactory.randomString(),
        username: String = StringTestFactory.randomString(),
        password: String = StringTestFactory.randomString(),
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
        primaryTotp = FakeKeyStoreCrypto.encrypt(primaryTotp),
        customFields = emptyList(),
        passkeys = passkeys
    )

    fun creditCard(
        cardNumber: String = StringTestFactory.randomString(),
        cardHolder: String = StringTestFactory.randomString(),
        expirationDate: String = StringTestFactory.randomString(),
        cvv: String = StringTestFactory.randomString(),
        pin: String = StringTestFactory.randomString()
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
