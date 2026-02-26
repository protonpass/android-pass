/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.securitycenter.microbenchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.domain.Item
import proton.android.pass.securitycenter.impl.checkers.RepeatedPasswordCheckerImpl
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.ItemTypeTestFactory

@RunWith(AndroidJUnit4::class)
class ReusedPasswordsCheckerImplBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private lateinit var instance: RepeatedPasswordCheckerImpl
    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider

    @Before
    fun setup() {
        encryptionContextProvider = FakeEncryptionContextProvider()
        instance = RepeatedPasswordCheckerImpl(
            encryptionContextProvider = encryptionContextProvider
        )
    }

    @Test
    fun checkRepeatedPasswords() {
        val items = generateDataset(numPasswords = 10, numItems = 10_000)
        benchmarkRule.measureRepeated {
            runBlocking(Dispatchers.Default) {
                instance.invoke(items)
            }
        }
    }

    private fun generateDataset(numPasswords: Int, numItems: Int): List<Item> {
        val passwords = encryptionContextProvider.withEncryptionContext {
            (0 until numPasswords).map {
                encrypt(StringTestFactory.randomString())
            }
        }

        return (0 until numItems).map { idx ->
            ItemTestFactory.random(ItemTypeTestFactory.login(password = passwords[idx % passwords.size]))
        }
    }
}
