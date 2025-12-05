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
import proton.android.pass.domain.ItemId
import proton.android.pass.securitycenter.api.passwords.RepeatedPasswordsGroup
import proton.android.pass.securitycenter.api.passwords.RepeatedPasswordsReport
import proton.android.pass.test.domain.ItemTestFactory

class RepeatedPasswordsReportTest {

    @Test
    fun `can handle empty report`() {
        val report = RepeatedPasswordsReport(emptyMap())
        assertThat(report.repeatedPasswordsCount).isEqualTo(0)
        assertThat(report.repeatedPasswordsGroups).isEmpty()
    }

    @Test
    fun `can handle report with single element`() {
        val key = "password"
        val item = ItemTestFactory.create(itemId = ItemId("1"))
        val report = RepeatedPasswordsReport(mapOf(key to listOf(item)))
        assertThat(report.repeatedPasswordsCount).isEqualTo(1)

        val expected = RepeatedPasswordsGroup(key, listOf(item))
        assertThat(report.repeatedPasswordsGroups).isEqualTo(listOf(expected))
    }

    @Test
    fun `sorts reports by count`() {
        val key1 = "password1"
        val key2 = "password2"

        val item11 = ItemTestFactory.create(itemId = ItemId("11"))
        val item12 = ItemTestFactory.create(itemId = ItemId("12"))
        val item21 = ItemTestFactory.create(itemId = ItemId("21"))
        val item22 = ItemTestFactory.create(itemId = ItemId("22"))
        val item23 = ItemTestFactory.create(itemId = ItemId("23"))

        val reportData = mapOf(
            key1 to listOf(item11, item12),
            key2 to listOf(item21, item22, item23)
        )
        val report = RepeatedPasswordsReport(reportData)
        assertThat(report.repeatedPasswordsCount).isEqualTo(2)

        val expected = listOf(
            RepeatedPasswordsGroup(key2, listOf(item21, item22, item23)),
            RepeatedPasswordsGroup(key1, listOf(item11, item12))
        )
        assertThat(report.repeatedPasswordsGroups).isEqualTo(expected)
    }

    @Test
    fun `can handle two groups with the same number of items`() {
        val key1 = "password1"
        val key2 = "password2"

        val item11 = ItemTestFactory.create(itemId = ItemId("11"))
        val item12 = ItemTestFactory.create(itemId = ItemId("12"))
        val item21 = ItemTestFactory.create(itemId = ItemId("21"))
        val item22 = ItemTestFactory.create(itemId = ItemId("22"))

        val reportData = mapOf(
            key1 to listOf(item11, item12),
            key2 to listOf(item21, item22)
        )
        val report = RepeatedPasswordsReport(reportData)
        assertThat(report.repeatedPasswordsCount).isEqualTo(2)

        val expected = listOf(
            RepeatedPasswordsGroup(key2, listOf(item21, item22)),
            RepeatedPasswordsGroup(key1, listOf(item11, item12))
        )
        assertThat(report.repeatedPasswordsGroups).containsExactlyElementsIn(expected)
    }

}

