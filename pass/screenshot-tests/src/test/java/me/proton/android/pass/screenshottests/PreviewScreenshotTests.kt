package me.proton.android.pass.screenshottests

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.airbnb.android.showkase.models.Showkase
import com.airbnb.android.showkase.models.ShowkaseBrowserComponent
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

data class ComponentPreview(
    private val showkaseBrowserComponent: ShowkaseBrowserComponent
) {
    val content: @Composable () -> Unit = showkaseBrowserComponent.component
    override fun toString(): String = showkaseBrowserComponent.componentKey
}

@RunWith(TestParameterInjector::class)
class PreviewScreenshotTests {

    object PreviewProvider : TestParameter.TestParameterValuesProvider {
        override fun provideValues(): List<ComponentPreview> =
            Showkase.getMetadata().componentList.map(::ComponentPreview)
    }

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.NEXUS_5
    )

    @Test
    fun preview_tests(
        @TestParameter(valuesProvider = PreviewProvider::class) componentPreview: ComponentPreview
    ) {
        paparazzi.snapshot {
            Box {
                componentPreview.content()
            }
        }
    }
}
