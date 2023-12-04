/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.screenshottests

import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.RenderSession
import org.junit.rules.RunRules
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.Statement
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class PaparazziWrapperRule internal constructor(
    val paparazzi: Paparazzi,
) : TestRule {

    override fun apply(base: Statement, description: Description): Statement =
        RunRules(
            base,
            listOf(PaparazziCleanupRule(paparazzi), paparazzi),
            description,
        )
}

/**
 * Taken from https://github.com/cashapp/paparazzi/issues/915#issuecomment-1783114569
 * There's a bug in Paparazzi that causes a memory leak when using Compose.
 * This rule is a workaround for that bug.
 * It should be once Paparazzi references the layoutlib from Iguana
 */
private class PaparazziCleanupRule(
    private val paparazzi: Paparazzi,
) : TestWatcher() {

    override fun finished(description: Description?) {
        super.finished(description)
        @Suppress("UNCHECKED_CAST")
        val renderSession: RenderSession = (
            paparazzi::class.memberProperties
                .first { it.name == "bridgeRenderSession" } as KProperty1<Paparazzi, RenderSession>
            )
            .apply { isAccessible = true }
            .invoke(paparazzi)
        renderSession.disposeHack()
    }
}
