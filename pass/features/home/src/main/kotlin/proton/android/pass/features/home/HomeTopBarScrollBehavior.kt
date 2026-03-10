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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

internal data class TopBarExpansionResult(
    val consumedY: Float,
    val newHeightOffset: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberHomeTopBarScrollConnection(scrollBehavior: TopAppBarScrollBehavior): NestedScrollConnection =
    remember(scrollBehavior) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return when {
                    available.y < 0f -> scrollBehavior.nestedScrollConnection.onPreScroll(available, source)
                    available.y > 0f && scrollBehavior.state.collapsedFraction > 0f -> {
                        val expansion = consumeTopBarExpansion(
                            availableY = available.y,
                            currentHeightOffset = scrollBehavior.state.heightOffset,
                            heightOffsetLimit = scrollBehavior.state.heightOffsetLimit
                        )
                        scrollBehavior.state.heightOffset = expansion.newHeightOffset
                        if (expansion.consumedY > 0f) {
                            Offset(x = 0f, y = expansion.consumedY)
                        } else {
                            Offset.Zero
                        }
                    }
                    else -> Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset = Offset.Zero

            override suspend fun onPreFling(available: Velocity): Velocity =
                scrollBehavior.nestedScrollConnection.onPreFling(available)

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity =
                scrollBehavior.nestedScrollConnection.onPostFling(consumed, available)
        }
    }

internal fun consumeTopBarExpansion(
    availableY: Float,
    currentHeightOffset: Float,
    heightOffsetLimit: Float
): TopBarExpansionResult {
    if (availableY <= 0f || currentHeightOffset >= 0f) {
        return TopBarExpansionResult(consumedY = 0f, newHeightOffset = currentHeightOffset)
    }

    val newHeightOffset = (currentHeightOffset + availableY).coerceIn(heightOffsetLimit, 0f)
    val consumedY = newHeightOffset - currentHeightOffset

    return TopBarExpansionResult(
        consumedY = consumedY.coerceAtLeast(0f),
        newHeightOffset = newHeightOffset
    )
}
