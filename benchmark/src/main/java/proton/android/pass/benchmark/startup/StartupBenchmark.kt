package proton.android.pass.benchmark.startup

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.benchmark.TARGET_PACKAGE
import proton.android.pass.benchmark.TIMEOUT

@LargeTest
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupNoCompilation() = startup(CompilationMode.None())

    @Test
    fun startupBaselineProfile() = startup(CompilationMode.Partial())

    @Test
    fun startupFullCompilation() = startup(CompilationMode.Full())

    private fun startup(compilationMode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = compilationMode,
        iterations = 10,
        startupMode = StartupMode.COLD,
        setupBlock = { pressHome() }
    ) {
        // Waits for the first rendered frame, which represents time to initial display.
        startActivityAndWait()

        // Waits for content to be visible, which represents time to fully drawn.
        device.wait(Until.hasObject(By.text("Iniciar sesión")), TIMEOUT)
    }
}
