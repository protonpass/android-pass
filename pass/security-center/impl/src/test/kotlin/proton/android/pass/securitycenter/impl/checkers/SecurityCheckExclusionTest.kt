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

package proton.android.pass.securitycenter.impl.checkers

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemFlag
import proton.android.pass.securitycenter.api.SecurityCheck
import proton.android.pass.securitycenter.api.isCheckExcluded
import proton.android.pass.test.domain.ItemTestFactory

internal class SecurityCheckExclusionTest {

    @Test
    fun `item without any flag is monitored`() {
        assertThat(itemWith().isCheckExcluded(SecurityCheck.WeakPassword)).isFalse()
    }

    @Test
    fun `item with the check ignored is excluded from that check only`() {
        val item = itemWith(ItemFlag.SkipWeakPasswordCheck)

        assertThat(item.isCheckExcluded(SecurityCheck.WeakPassword)).isTrue()
        assertThat(item.isCheckExcluded(SecurityCheck.ReusedPassword)).isFalse()
    }

    @Test
    fun `globally excluded item without per check flags is excluded from every check`() {
        val item = itemWith(ItemFlag.SkipHealthCheck)

        SecurityCheck.entries.forEach { check ->
            assertThat(item.isCheckExcluded(check)).isTrue()
        }
    }

    @Test
    fun `globally excluded item reports the checks that were restored`() {
        val item = itemWith(ItemFlag.SkipHealthCheck, ItemFlag.Skip2FACheck)

        assertThat(item.isCheckExcluded(SecurityCheck.Missing2fa)).isTrue()
        assertThat(item.isCheckExcluded(SecurityCheck.WeakPassword)).isFalse()
        assertThat(item.isCheckExcluded(SecurityCheck.ReusedPassword)).isFalse()
    }

    @Test
    fun `globally excluded item keeps hiding the checks it cascaded`() {
        val item = itemWith(ItemFlag.SkipHealthCheck, ItemFlag.SkipWeakPasswordCheck)

        assertThat(item.isCheckExcluded(SecurityCheck.WeakPassword)).isTrue()
    }

    private fun itemWith(vararg flags: ItemFlag): Item = ItemTestFactory.createLogin(
        flags = flags.sumOf { flag -> flag.value }
    )
}
