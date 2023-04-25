package proton.android.pass.featurefeatureflags.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import proton.android.pass.preferences.FeatureFlags

@Composable
fun BoolFeatureFlagItem(
    modifier: Modifier = Modifier,
    featureFlag: FeatureFlags,
    value: Boolean,
    onToggle: (FeatureFlags, Boolean) -> Unit
) {
    Row(modifier.padding(16.dp)) {
        Column(modifier.weight(1f)) {
            Text(text = featureFlag.title)
            Text(text = featureFlag.description)
        }
        Switch(
            checked = value,
            onCheckedChange = { onToggle(featureFlag, it) }
        )
    }
}
