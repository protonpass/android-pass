package proton.android.pass.featurehome.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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

    private fun testScroll(compilationMode: CompilationMode) = benchmarkRule.measureRepeated(
        compilationMode = compilationMode,
        packageName = PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD,
        setupBlock = {
            pressHome()
            startActivityAndWait()
        }
    ) {
        val contentList = device.findObject(By.res(ITEMS_LIST_ID))
        val searchCondition = Until.hasObject(By.text(VISIBLE_ITEM))
        // Wait until the item list is loaded
        contentList.wait(searchCondition, TIMEOUT)

        // Set gesture margin to avoid triggering system gesture navigation
        contentList.setSafeGestureMargin(device)

        // Scroll down the list
        contentList.fling(Direction.DOWN)

        // Wait for the scroll to finish
        device.waitForIdle()
    }
}
