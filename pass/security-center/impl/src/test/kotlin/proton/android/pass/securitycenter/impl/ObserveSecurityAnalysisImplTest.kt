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

package proton.android.pass.securitycenter.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.fakes.FakeAppDispatchers
import proton.android.pass.data.fakes.usecases.items.FakeObserveMonitoredItems
import proton.android.pass.securitycenter.api.InsecurePasswordsResult
import proton.android.pass.securitycenter.api.Missing2faResult
import proton.android.pass.securitycenter.api.SecurityAnalysis
import proton.android.pass.securitycenter.api.passwords.InsecurePasswordsReport
import proton.android.pass.securitycenter.api.passwords.Missing2faReport
import proton.android.pass.securitycenter.fakes.mother.BreachDataResultTestFactory
import proton.android.pass.securitycenter.fakes.passwords.FakeBreachedDataChecker
import proton.android.pass.securitycenter.fakes.passwords.FakeInsecurePasswordChecker
import proton.android.pass.securitycenter.fakes.passwords.FakeMissing2faChecker
import proton.android.pass.securitycenter.fakes.passwords.FakeRepeatedPasswordChecker
import proton.android.pass.test.domain.ItemTestFactory

class ObserveSecurityAnalysisImplTest {

    private lateinit var instance: ObserveSecurityAnalysisImpl

    private lateinit var observeMonitoredItems: FakeObserveMonitoredItems
    private lateinit var repeatedPasswordChecker: FakeRepeatedPasswordChecker
    private lateinit var missing2faChecker: FakeMissing2faChecker
    private lateinit var insecurePasswordChecker: FakeInsecurePasswordChecker
    private lateinit var breachedDataChecker: FakeBreachedDataChecker

    @Before
    fun setup() {
        observeMonitoredItems = FakeObserveMonitoredItems()
        repeatedPasswordChecker = FakeRepeatedPasswordChecker()
        missing2faChecker = FakeMissing2faChecker()
        insecurePasswordChecker = FakeInsecurePasswordChecker()
        breachedDataChecker = FakeBreachedDataChecker()

        instance = ObserveSecurityAnalysisImpl(
            repeatedPasswordChecker = repeatedPasswordChecker,
            missing2faChecker = missing2faChecker,
            insecurePasswordChecker = insecurePasswordChecker,
            breachedDataChecker = breachedDataChecker,
            observeMonitoredItems = observeMonitoredItems,
            dispatchers = FakeAppDispatchers()
        )
    }

    @Test
    fun `emits all Loading on start`() = runTest {
        instance().test {
            val item = awaitItem()
            assertThat(item).isEqualTo(
                SecurityAnalysis(
                    breachedData = LoadingResult.Loading,
                    reusedPasswords = LoadingResult.Loading,
                    insecurePasswords = LoadingResult.Loading,
                    missing2fa = LoadingResult.Loading
                )
            )
        }
    }

    @Test
    fun `can emit partial errors`() = runTest {
        val missing2fa = Missing2faReport(emptyList())
        val breachData = BreachDataResultTestFactory.random()
        val insecure = InsecurePasswordsReport(emptyList(), emptyList())
        val error = IllegalStateException("test")
        repeatedPasswordChecker.setResult(Result.failure(error))
        missing2faChecker.setResult(missing2fa)
        breachedDataChecker.setResult(breachData)
        insecurePasswordChecker.setResult(insecure)

        val monitoredItems = listOf(
            ItemTestFactory.random(),
            ItemTestFactory.random()
        )

        observeMonitoredItems.emitMonitoredItems(monitoredItems)

        instance().test {
            skipItems(1) // Initial loading
            val item = awaitItem()
            assertThat(item).isEqualTo(
                SecurityAnalysis(
                    breachedData = LoadingResult.Success(breachData),
                    reusedPasswords = LoadingResult.Error(error),
                    insecurePasswords = LoadingResult.Success(InsecurePasswordsResult(0)),
                    missing2fa = LoadingResult.Success(Missing2faResult(0))
                )
            )
        }
    }

    @Test
    fun `checkers are properly called`() = runTest {
        val monitoredItems = listOf(
            ItemTestFactory.random(),
            ItemTestFactory.random()
        )
        observeMonitoredItems.emitMonitoredItems(monitoredItems)

        instance().test {
            skipItems(1) // Initial loading
            awaitItem()
        }

        val expectedItems = listOf(monitoredItems)

        assertThat(repeatedPasswordChecker.memory()).isEqualTo(expectedItems)
        assertThat(missing2faChecker.memory()).isEqualTo(expectedItems)
        assertThat(insecurePasswordChecker.memory()).isEqualTo(expectedItems)
        assertThat(breachedDataChecker.memory()).isEqualTo(expectedItems)
    }

}
