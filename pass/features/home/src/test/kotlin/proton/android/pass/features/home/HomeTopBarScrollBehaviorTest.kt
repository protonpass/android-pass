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

package proton.android.pass.features.home

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class HomeTopBarScrollBehaviorTest {

    @Test
    fun `consumes only remaining expansion distance on downward drag`() {
        val result = consumeTopBarExpansion(
            availableY = 100f,
            currentHeightOffset = -10f,
            heightOffsetLimit = -48f
        )

        assertThat(result.consumedY).isWithin(0.0001f).of(10f)
        assertThat(result.newHeightOffset).isWithin(0.0001f).of(0f)
    }

    @Test
    fun `does not consume downward drag when top bar is already expanded`() {
        val result = consumeTopBarExpansion(
            availableY = 100f,
            currentHeightOffset = 0f,
            heightOffsetLimit = -48f
        )

        assertThat(result.consumedY).isWithin(0.0001f).of(0f)
        assertThat(result.newHeightOffset).isWithin(0.0001f).of(0f)
    }
}
