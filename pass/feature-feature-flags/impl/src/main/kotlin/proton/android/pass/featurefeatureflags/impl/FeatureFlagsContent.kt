package proton.android.pass.featurefeatureflags.impl

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.preferences.FeatureFlag

@Composable
fun FeatureFlagsContent(
    modifier: Modifier = Modifier,
    state: Map<FeatureFlag, Any>,
    onToggle: (FeatureFlag, Boolean) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar { Text(text = "Feature Flags") } }
    ) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            state.forEach { (featureFlag, value) ->
                when (value) {
                    is Boolean -> item {
                        BoolFeatureFlagItem(
                            featureFlag = featureFlag,
                            value = value,
                            onToggle = onToggle
                        )
                    }
                }
            }
        }
    }
}
