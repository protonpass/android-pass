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

package proton.android.pass.features.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.commonui.api.TestTags.HOME_EMPTY_TAG
import proton.android.pass.commonui.api.TestTags.HOME_LOADING_TAG
import proton.android.pass.features.ITERATIONS
import proton.android.pass.features.PACKAGE_NAME
import proton.android.pass.features.TIMEOUT
import proton.android.pass.features.setSafeGestureMargin

@Suppress("MagicNumber")
@RunWith(AndroidJUnit4::class)
class HomeScrollBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollNoCompilation() = testScroll(CompilationMode.None())

    @Test
    fun scrollBaselineProfile() = testScroll(CompilationMode.Partial())

    @Test
    fun scrollFullCompilation() = testScroll(CompilationMode.Full())

    private fun testScroll(compilationMode: CompilationMode) {
        var firstStart = true
        benchmarkRule.measureRepeated(
            compilationMode = compilationMode,
            packageName = PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            iterations = ITERATIONS,
            setupBlock = {
                if (firstStart) {
                    pressHome()
                    startActivityAndWait()
                    firstStart = false
                    device.wait(Until.gone(By.res(HOME_EMPTY_TAG)), TIMEOUT)
                    device.wait(Until.gone(By.res(HOME_LOADING_TAG)), TIMEOUT)
                    device.wait(Until.hasObject(By.scrollable(true)), TIMEOUT)
                }
            }
        ) {
            val contentList = device.findObject(By.scrollable(true))
            if (contentList == null) {
                TestCase.fail("No scrollable view found in hierarchy")
            }
            // Set gesture margin to avoid triggering system gesture navigation
            contentList.setSafeGestureMargin(device)

            contentList.fling(Direction.DOWN)
            contentList.fling(Direction.UP)

            // Wait for the scroll to finish
            device.waitForIdle()
        }
    }
}
