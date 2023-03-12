package proton.android.pass.benchmark.baseline

import androidx.benchmark.macro.ExperimentalBaselineProfilesApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import proton.android.pass.benchmark.TARGET_PACKAGE
import proton.android.pass.benchmark.TIMEOUT

@ExperimentalBaselineProfilesApi
class StartupOnlyBaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun appStartupOnly() {
        baselineProfileRule.collectBaselineProfile(TARGET_PACKAGE) {
            startActivityAndWait()
            device.wait(Until.hasObject(By.text("Sign in")), TIMEOUT)
        }
    }
}
