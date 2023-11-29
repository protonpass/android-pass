package proton.android.pass.featurehome.macrobenchmark

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
    fun startup() = benchmarkRule.measureRepeated(
        packageName = "proton.android.pass.featurehome.demoapp",
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD,
        setupBlock = {
            pressHome()
            startActivityAndWait()
        }
    ) {
        val contentList = device.findObject(By.res("itemsList"))
        val searchCondition = Until.hasObject(By.text("Login 49"))
        // Wait until the item list is loaded
        contentList.wait(searchCondition, 5_000)

        // Set gesture margin to avoid triggering system gesture navigation
        contentList.setGestureMargin(device.displayWidth / 5)

        // Scroll down the list
        contentList.fling(Direction.DOWN)

        // Wait for the scroll to finish
        device.waitForIdle()
    }
}
