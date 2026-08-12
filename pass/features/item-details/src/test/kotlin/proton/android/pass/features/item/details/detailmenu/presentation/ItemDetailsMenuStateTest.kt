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
    fun `healthy item with no skipped check offers exclude`() {
        val state = stateOf(item = itemWith())

        assertThat(state.isItemExcludedFromMonitoring).isFalse()
    }

    @Test
    fun `item with a single skipped check offers include`() {
        val state = stateOf(item = itemWith(ItemFlag.SkipReusedPasswordCheck))

        assertThat(state.isItemExcludedFromMonitoring).isTrue()
    }

    @Test
    fun `item with several skipped checks offers include`() {
        val state = stateOf(
            item = itemWith(ItemFlag.SkipReusedPasswordCheck, ItemFlag.SkipCompromisedPasswordCheck)
        )

        assertThat(state.isItemExcludedFromMonitoring).isTrue()
    }

    @Test
    fun `globally excluded item offers include`() {
        val state = stateOf(item = itemWith(ItemFlag.SkipHealthCheck))

        assertThat(state.isItemExcludedFromMonitoring).isTrue()
    }

    @Test
    fun `item keeps offering include while a skipped check remains after a restore`() {
        val state = stateOf(item = itemWith(ItemFlag.SkipWeakPasswordCheck))

        assertThat(state.isItemExcludedFromMonitoring).isTrue()
    }

    @Test
    fun `item offers exclude again once the last skipped check is restored`() {
        val state = stateOf(item = itemWith())

        assertThat(state.isItemExcludedFromMonitoring).isFalse()
    }

    @Test
    fun `flags unrelated to monitoring do not offer include`() {
        val state = stateOf(item = itemWith(ItemFlag.HasAttachments))

        assertThat(state.isItemExcludedFromMonitoring).isFalse()
    }

    @Test
    fun `per check flags are ignored when the feature flag is off`() {
        val state = stateOf(
            item = itemWith(ItemFlag.SkipReusedPasswordCheck),
            isPerCheckExclusionEnabled = false
        )

        assertThat(state.isItemExcludedFromMonitoring).isFalse()
    }

    @Test
    fun `globally excluded item still offers include when the feature flag is off`() {
        val state = stateOf(
            item = itemWith(ItemFlag.SkipHealthCheck),
            isPerCheckExclusionEnabled = false
        )

        assertThat(state.isItemExcludedFromMonitoring).isTrue()
    }

    private fun itemWith(vararg flags: ItemFlag): Item = ItemTestFactory.createLogin(
        flags = flags.sumOf { flag -> flag.value }
    )

    private fun stateOf(item: Item, isPerCheckExclusionEnabled: Boolean = true): ItemDetailsMenuState =
        ItemDetailsMenuState(
            action = BottomSheetItemAction.None,
            event = ItemDetailsMenuEvent.Idle,
            itemOption = item.some(),
            itemActionsOption = None,
            shareOption = None,
            isPerCheckExclusionEnabled = isPerCheckExclusionEnabled
        )
}
