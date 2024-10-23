package proton.android.pass.featurehome.macrobenchmark

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
import proton.android.pass.featurehome.ITERATIONS
import proton.android.pass.featurehome.PACKAGE_NAME
import proton.android.pass.featurehome.TIMEOUT
import proton.android.pass.featurehome.setSafeGestureMargin

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
