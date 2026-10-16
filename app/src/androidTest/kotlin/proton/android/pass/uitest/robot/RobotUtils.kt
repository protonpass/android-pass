/*
 * Copyright (c) 2026 Proton AG.
 * This file is part of Proton Pass.
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

package proton.android.pass.uitest.robot

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.SemanticsMatcher
import androidx.test.platform.app.InstrumentationRegistry
import me.proton.test.fusion.FusionConfig

const val TIMEOUT_MILLIS = 30_000L

private const val POSITION_POLL_MILLIS = 300L

val composeRule get() = FusionConfig.Compose.testRule.get()

fun exists(matcher: SemanticsMatcher): Boolean =
    composeRule.onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

fun string(resId: Int, vararg formatArgs: Any): String =
    InstrumentationRegistry.getInstrumentation().targetContext.getString(resId, *formatArgs)

fun stringIdentifier(name: String): Int {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    return context.resources.getIdentifier(name, "string", context.packageName)
        .also { check(it != 0) { "$name string not found" } }
}

fun waitUntilPositionStable(matcher: SemanticsMatcher) {
    var previous: Rect? = null
    composeRule.waitUntil(TIMEOUT_MILLIS) {
        val current = composeRule.onNode(matcher, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        val stable = current == previous
        previous = current
        Thread.sleep(POSITION_POLL_MILLIS)
        stable
    }
}
