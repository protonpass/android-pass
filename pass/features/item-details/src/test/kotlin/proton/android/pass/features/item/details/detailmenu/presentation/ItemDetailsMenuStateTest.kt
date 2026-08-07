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

package proton.android.pass.features.item.details.detailmenu.presentation

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemFlag
import proton.android.pass.test.domain.ItemTestFactory

internal class ItemDetailsMenuStateTest {

    @Test
    fun `healthy item with no alerts offers exclude`() {
        val state = stateOf(item = itemWith())

        assertThat(state.isItemExcludedFromMonitoring).isFalse()
    }

    @Test
    fun `item with alerts and none skipped offers exclude`() {
        val state = stateOf(
            item = itemWith(),
            triggeredChecks = setOf(ItemFlag.SkipWeakPasswordCheck, ItemFlag.SkipReusedPasswordCheck)
        )

        assertThat(state.isItemExcludedFromMonitoring).isFalse()
    }

    @Test
    fun `item with some alerts skipped offers exclude when not opened from excluded section`() {
        val state = stateOf(
            item = itemWith(ItemFlag.SkipReusedPasswordCheck),
            triggeredChecks = setOf(
                ItemFlag.SkipReusedPasswordCheck,
                ItemFlag.SkipWeakPasswordCheck
            )
        )

        assertThat(state.isItemExcludedFromMonitoring).isFalse()
    }

    @Test
    fun `item with some alerts skipped offers include when opened from excluded section`() {
        val state = stateOf(
            item = itemWith(ItemFlag.SkipReusedPasswordCheck),
            triggeredChecks = setOf(
                ItemFlag.SkipReusedPasswordCheck,
                ItemFlag.SkipWeakPasswordCheck
            ),
            isOpenedFromExcludedSection = true
        )

        assertThat(state.isItemExcludedFromMonitoring).isTrue()
    }

    @Test
    fun `item with all alerts skipped offers include from any view`() {
        val item = itemWith(ItemFlag.SkipReusedPasswordCheck, ItemFlag.SkipCompromisedPasswordCheck)
        val triggered = setOf(ItemFlag.SkipReusedPasswordCheck, ItemFlag.SkipCompromisedPasswordCheck)

        assertThat(stateOf(item, triggered).isItemExcludedFromMonitoring).isTrue()
        assertThat(
            stateOf(item, triggered, isOpenedFromExcludedSection = true).isItemExcludedFromMonitoring
        ).isTrue()
    }

    @Test
    fun `globally excluded item offers include from any view`() {
        val item = itemWith(ItemFlag.SkipHealthCheck)

        assertThat(stateOf(item).isItemExcludedFromMonitoring).isTrue()
        assertThat(stateOf(item, isOpenedFromExcludedSection = true).isItemExcludedFromMonitoring).isTrue()
    }

    @Test
    fun `excluded section with no skipped check offers exclude`() {
        val state = stateOf(
            item = itemWith(),
            triggeredChecks = setOf(ItemFlag.SkipWeakPasswordCheck),
            isOpenedFromExcludedSection = true
        )

        assertThat(state.isItemExcludedFromMonitoring).isFalse()
    }

    private fun itemWith(vararg flags: ItemFlag): Item = ItemTestFactory.createLogin(
        flags = flags.sumOf { flag -> flag.value }
    )

    private fun stateOf(
        item: Item,
        triggeredChecks: Set<ItemFlag> = emptySet(),
        isOpenedFromExcludedSection: Boolean = false
    ): ItemDetailsMenuState = ItemDetailsMenuState(
        action = BottomSheetItemAction.None,
        event = ItemDetailsMenuEvent.Idle,
        itemOption = item.some(),
        itemActionsOption = None,
        shareOption = None,
        triggeredChecks = triggeredChecks,
        isOpenedFromExcludedSection = isOpenedFromExcludedSection
    )
}
