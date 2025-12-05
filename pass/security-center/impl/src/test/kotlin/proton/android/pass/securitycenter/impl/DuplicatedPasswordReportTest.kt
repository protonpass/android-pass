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

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.domain.Item
import proton.android.pass.securitycenter.api.passwords.DuplicatedPasswordReport
import proton.android.pass.test.domain.ItemTestFactory

internal class DuplicatedPasswordReportTest {

    @Test
    internal fun `GIVEN no password duplications WHEN creating report THEN handle no duplications`() {
        val duplicatedPasswordItems = emptySet<Item>()
        val expectedDuplicationsCount = 0
        val expectedHasDuplications = false
        val expectedDuplications = emptyList<Item>()

        val report = DuplicatedPasswordReport(duplicatedPasswordItems)

        assertThat(report.duplicationCount).isEqualTo(expectedDuplicationsCount)
        assertThat(report.hasDuplications).isEqualTo(expectedHasDuplications)
        assertThat(report.duplications).isEqualTo(expectedDuplications)
    }

    @Test
    internal fun `GIVEN password duplications WHEN creating report THEN handle duplications`() {
        val duplicatedPasswordItem1 = ItemTestFactory.random()
        val duplicatedPasswordItem2 = ItemTestFactory.random()
        val duplicatedPasswordItems = setOf(duplicatedPasswordItem1, duplicatedPasswordItem2)
        val expectedDuplicationsCount = 2
        val expectedHasDuplications = true
        val expectedDuplications = listOf(duplicatedPasswordItem1, duplicatedPasswordItem2)

        val report = DuplicatedPasswordReport(duplicatedPasswordItems)

        assertThat(report.duplicationCount).isEqualTo(expectedDuplicationsCount)
        assertThat(report.hasDuplications).isEqualTo(expectedHasDuplications)
        assertThat(report.duplications).isEqualTo(expectedDuplications)
    }

}
