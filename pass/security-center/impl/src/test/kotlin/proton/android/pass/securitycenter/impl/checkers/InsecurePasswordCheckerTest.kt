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

package proton.android.pass.securitycenter.impl.checkers

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.commonrust.api.PasswordScore
import proton.android.pass.commonrust.fakes.FakePasswordScorer
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestItemType

class InsecurePasswordCheckerTest {

    private lateinit var instance: InsecurePasswordCheckerImpl

    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider
    private lateinit var passwordScorer: FakePasswordScorer

    @Before
    fun setup() {
        encryptionContextProvider = FakeEncryptionContextProvider()
        passwordScorer = FakePasswordScorer()
        instance = InsecurePasswordCheckerImpl(
            encryptionContextProvider = encryptionContextProvider,
            passwordScorer = passwordScorer
        )
    }

    @Test
    fun `can handle empty list`() = runTest {
        val report = instance.invoke(emptyList())
        assertThat(report.insecurePasswordsCount).isEqualTo(0)
    }

    @Test
    fun `does not report any item if all are strong`() = runTest {
        val password = "testPassword"
        passwordScorer.defineScore(password, PasswordScore.STRONG)

        val items = (0 until 10).map {
            TestItem.create(
                itemType = TestItemType.login(
                    password = encryptionContextProvider.withEncryptionContext { encrypt(password) }
                )
            )
        }

        val report = instance.invoke(items)
        assertThat(report.insecurePasswordsCount).isEqualTo(0)
    }

    @Test
    fun `can report weak passwords`() = runTest {
        val strongPassword = "testPassword"
        val weakPassword = "weakPassword"
        passwordScorer.defineScore(strongPassword, PasswordScore.STRONG)
        passwordScorer.defineScore(weakPassword, PasswordScore.WEAK)

        val strongItems = generateItemsWithPassword(
            password = strongPassword,
            prefix = "STRONG",
            count = 5
        )

        val weakItems = generateItemsWithPassword(
            password = weakPassword,
            prefix = "WEAK",
            count = 5
        )

        val report = instance.invoke(strongItems + weakItems)
        assertThat(report.insecurePasswordsCount).isEqualTo(5)
        assertThat(report.weakPasswordItems).isEqualTo(weakItems)
        assertThat(report.vulnerablePasswordItems).isEmpty()
    }

    @Test
    fun `can report vulnerable passwords`() = runTest {
        val strongPassword = "testPassword"
        val vulnerablePassword = "vulnerablePassword"
        passwordScorer.defineScore(strongPassword, PasswordScore.STRONG)
        passwordScorer.defineScore(vulnerablePassword, PasswordScore.VULNERABLE)

        val strongItems = generateItemsWithPassword(
            password = strongPassword,
            prefix = "STRONG",
            count = 5
        )

        val vulnerableItems = generateItemsWithPassword(
            password = vulnerablePassword,
            prefix = "VULNERABLE",
            count = 5
        )

        val report = instance.invoke(strongItems + vulnerableItems)
        assertThat(report.insecurePasswordsCount).isEqualTo(5)
        assertThat(report.vulnerablePasswordItems).isEqualTo(vulnerableItems)
        assertThat(report.weakPasswordItems).isEmpty()
    }

    @Test
    fun `can report vulnerable and weak passwords`() = runTest {
        val strongPassword = "testPassword"
        val vulnerablePassword = "vulnerablePassword"
        val weakPassword = "weakPassword"
        passwordScorer.defineScore(strongPassword, PasswordScore.STRONG)
        passwordScorer.defineScore(vulnerablePassword, PasswordScore.VULNERABLE)
        passwordScorer.defineScore(weakPassword, PasswordScore.WEAK)

        val strongItems = generateItemsWithPassword(
            password = strongPassword,
            prefix = "STRONG",
            count = 10
        )

        val weakItems = generateItemsWithPassword(
            password = weakPassword,
            prefix = "WEAK",
            count = 5
        )
        val vulnerableItems = generateItemsWithPassword(
            password = vulnerablePassword,
            prefix = "VULNERABLE",
            count = 5
        )

        val report = instance.invoke(strongItems + vulnerableItems + weakItems)
        assertThat(report.insecurePasswordsCount).isEqualTo(weakItems.size + vulnerableItems.size)
        assertThat(report.vulnerablePasswordItems).isEqualTo(vulnerableItems)
        assertThat(report.weakPasswordItems).isEqualTo(weakItems)
    }

    private fun generateItemsWithPassword(
        password: String,
        prefix: String,
        count: Int
    ): List<Item> = (0 until count).map { idx ->
        TestItem.create(
            itemId = ItemId("$prefix-$idx"),
            itemType = TestItemType.login(
                password = encryptionContextProvider.withEncryptionContext {
                    encrypt(password)
                }
            )
        )
    }


}
